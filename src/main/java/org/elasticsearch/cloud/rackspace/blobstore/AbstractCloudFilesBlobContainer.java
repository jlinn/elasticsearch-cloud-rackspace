package org.elasticsearch.cloud.rackspace.blobstore;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.support.AbstractBlobContainer;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: Joe Linn
 * Date: 3/3/14
 * Time: 12:09 PM
 */
abstract public class AbstractCloudFilesBlobContainer extends AbstractBlobContainer{
    protected final CloudFilesBlobStore blobStore;

    protected final String keyPath;

    protected AbstractCloudFilesBlobContainer(BlobPath path, CloudFilesBlobStore blobStore) {
        super(path);
        this.blobStore = blobStore;
        String keyPath = path.buildAsString("/");
        if(!keyPath.isEmpty() && !keyPath.substring(keyPath.length() - 1).equals("/")){
            keyPath = keyPath + "/";
        }
        this.keyPath = keyPath;
    }

    @Override
    public boolean blobExists(String blobName) {
        return getRegionBlobStore().blobExists(blobStore.getContainer(), buildKey(blobName));
    }

    @Override
    public void readBlob(final String blobName, final ReadBlobListener listener) {
        blobStore.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                InputStream is;
                try {
                    Blob blob = getRegionBlobStore().getBlob(blobStore.getContainer(), buildKey(blobName));
                    is = blob.getPayload().openStream();
                } catch (IOException e) {
                    listener.onFailure(e);
                    return;
                }
                byte[] buffer = new byte[blobStore.getBufferSizeInBytes()];
                int bytesRead;
                try {
                    while((bytesRead = is.read(buffer)) != -1){
                        listener.onPartial(buffer, 0, bytesRead);
                    }
                    listener.onCompleted();
                } catch (IOException e) {
                    try {
                        is.close();
                    } catch (IOException ignored) {

                    }
                    listener.onFailure(e);
                }
            }
        });
    }

    @Override
    public boolean deleteBlob(String blobName) throws IOException {
        getRegionBlobStore().removeBlob(blobStore.getContainer(), buildKey(blobName));
        return true;
    }

    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs() throws IOException {
        return listBlobsByPrefix(null);
    }

    @Override
    public ImmutableMap<String, BlobMetaData> listBlobsByPrefix(@Nullable String blobNamePrefix) throws IOException {
        ImmutableMap.Builder<String, BlobMetaData> blobsBuilder = ImmutableMap.builder();
        PageSet<? extends StorageMetadata> list = null;
        String marker = null;
        while(true){
            if(list != null){
                marker = list.getNextMarker();
            }
            ListContainerOptions options;
            if(!keyPath.equals("/")){
                options = ListContainerOptions.Builder.inDirectory(keyPath);
            }
            else{
                options = new ListContainerOptions();
            }
            if(marker != null){
                // continuation of a previously started list operation
                options.afterMarker(marker);
            }
            list = getRegionBlobStore().list(blobStore.getContainer(), options);
            for(StorageMetadata item : list){
                String name = item.getName();
                if(!keyPath.equals("/")){
                    name = name.substring(keyPath.length());
                }
                long length = 0;
                if(item.getUserMetadata().containsKey("length")){
                    length = Long.valueOf(item.getUserMetadata().get("length"));
                }
                if((blobNamePrefix != null && name.startsWith(blobNamePrefix) || blobNamePrefix == null)){
                    blobsBuilder.put(name, new PlainBlobMetaData(name, length));
                }
            }

            if(list.getNextMarker() == null){
                // we have the whole list
                break;
            }
        }

        return blobsBuilder.build();
    }

    protected String buildKey(String blobName){
        return keyPath + blobName;
    }

    protected BlobStore getRegionBlobStore(){
        return blobStore.getBlobStoreContext().getBlobStore(blobStore.getLocation().getId());
    }
}
