package org.elasticsearch.cloud.rackspace.blobstore;

import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.jclouds.blobstore.domain.Blob;

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
    protected CloudFilesImmutableBlobContainer(BlobPath path, CloudFilesBlobStore blobStore) {
        super(path, blobStore);
    }

    @Override
    public void writeBlob(final String blobName, final InputStream is, final long sizeInBytes, final WriterListener listener) {
        blobStore.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> metadata = new HashMap<String, String>();
                metadata.put("length", String.valueOf(sizeInBytes));
                Blob blob = blobStore.getBlobStoreContext().getBlobStore().blobBuilder(buildKey(blobName)).userMetadata(metadata)
                        .payload(is).contentLength(sizeInBytes).build();
                blobStore.getBlobStoreContext().getBlobStore().putBlob(blobStore.getContainer(), blob);
                listener.onCompleted();
            }
        });
    }

    @Override
    public void writeBlob(String blobName, InputStream is, long sizeInBytes) throws IOException {
        Blob blob = blobStore.getBlobStoreContext().getBlobStore().blobBuilder(buildKey(blobName)).payload(is).contentLength(sizeInBytes).build();
        blobStore.getBlobStoreContext().getBlobStore().putBlob(blobStore.getContainer(), blob);
    }
}
