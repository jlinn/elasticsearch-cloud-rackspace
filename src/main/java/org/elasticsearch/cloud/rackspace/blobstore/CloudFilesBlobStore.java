package org.elasticsearch.cloud.rackspace.blobstore;

import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStore;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.domain.Location;

import java.util.concurrent.Executor;

/**
 * User: Joe Linn
 * Date: 2/12/14
 * Time: 4:01 PM
 */
public class CloudFilesBlobStore extends AbstractComponent implements BlobStore{
    private final BlobStoreContext blobStoreContext;

    private final String container;

    private final Executor executor;

    private final int bufferSizeInBytes;

    public CloudFilesBlobStore(Settings settings, BlobStoreContext context, String container, Location location, Executor executor) {
        super(settings);
        this.blobStoreContext = context;
        this.container = container;
        this.executor = executor;

        this.bufferSizeInBytes = (int) settings.getAsBytesSize("buffer_size", new ByteSizeValue(100, ByteSizeUnit.KB)).bytes();

        // create the cloud files container if it does not already exist
        blobStoreContext.getBlobStore().createContainerInLocation(location, container);
    }

    public BlobStoreContext getBlobStoreContext() {
        return blobStoreContext;
    }

    public String getContainer() {
        return container;
    }

    public Executor getExecutor() {
        return executor;
    }

    public int getBufferSizeInBytes() {
        return bufferSizeInBytes;
    }

    @Override
    public ImmutableBlobContainer immutableBlobContainer(BlobPath path) {
        return new CloudFilesImmutableBlobContainer(path, this);
    }

    @Override
    public void delete(BlobPath path) {
        blobStoreContext.getBlobStore().deleteDirectory(container, path.buildAsString("/"));
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return container;
    }
}
