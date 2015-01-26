package org.elasticsearch.cloud.rackspace.blobstore;

import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Joe Linn
 * Date: 3/3/14
 * Time: 2:33 PM
 */
public class CloudFilesImmutableBlobContainer extends AbstractCloudFilesBlobContainer implements ImmutableBlobContainer{
    private static final Logger logger = LoggerFactory.getLogger(CloudFilesImmutableBlobContainer.class);

    protected CloudFilesImmutableBlobContainer(BlobPath path, CloudFilesBlobStore blobStore) {
        super(path, blobStore);
    }

    @Override
    public void writeBlob(final String blobName, final InputStream is, final long sizeInBytes, final WriterListener listener) {
        blobStore.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if(logger.isDebugEnabled()){
                    logger.debug(String.format("Preparing to write blob named %s with size %s asynchronously", blobName, sizeInBytes));
                }
                try {
                    getRegionBlobStore().putBlob(blobStore.getContainer(), buildBlob(blobName, is, sizeInBytes));
                } catch (ContainerNotFoundException e) {
                    listener.onFailure(e);
                    return;
                }
                listener.onCompleted();
                if(logger.isDebugEnabled()){
                    logger.debug(String.format("Successfully wrote blob %s asynchronously", blobName));
                }
            }
        });
    }

    @Override
    public void writeBlob(String blobName, InputStream is, long sizeInBytes) throws IOException {
        if(logger.isDebugEnabled()){
            logger.debug(String.format("Preparing to write blob named %s with size %s", blobName, sizeInBytes));
        }
        try {
            getRegionBlobStore().putBlob(blobStore.getContainer(), buildBlob(blobName, is, sizeInBytes));
        } catch (ContainerNotFoundException e) {
            throw new IOException(String.format("Container not found when attempting to store blob named %s", blobName), e);
        }
        if(logger.isDebugEnabled()){
            logger.debug(String.format("Successfully wrote blob %s", blobName));
        }
    }


    private Blob buildBlob(final String blobName, final InputStream is, final long sizeInBytes){
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("length", String.valueOf(sizeInBytes));
        BlobStore regionBlobStore = getRegionBlobStore();
        return regionBlobStore.blobBuilder(buildKey(blobName))
                .userMetadata(metadata)
                .payload(is)
                .contentLength(sizeInBytes)
                .build();
    }
}
