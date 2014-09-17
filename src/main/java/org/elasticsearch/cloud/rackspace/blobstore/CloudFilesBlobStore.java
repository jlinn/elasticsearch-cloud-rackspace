package org.elasticsearch.cloud.rackspace.blobstore;

import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStore;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.jclouds.domain.Location;
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;

import java.util.concurrent.Executor;

import static com.google.inject.internal.util.$Iterables.getOnlyElement;

/**
 * User: Joe Linn
 * Date: 2/12/14
 * Time: 4:01 PM
 */
public class CloudFilesBlobStore extends AbstractComponent implements BlobStore{
    private final RegionScopedBlobStoreContext blobStoreContext;

    private final String container;

    private final Executor executor;

    private final int bufferSizeInBytes;

    private final Location location;

    public CloudFilesBlobStore(Settings settings, RegionScopedBlobStoreContext context, String container, Location location, Executor executor) {
        super(settings);
        this.blobStoreContext = context;
        this.container = container;
        this.executor = executor;
        this.location = location;

        this.bufferSizeInBytes = (int) settings.getAsBytesSize("buffer_size", new ByteSizeValue(100, ByteSizeUnit.KB)).bytes();

        // create the cloud files container if it does not already exist
        org.jclouds.blobstore.BlobStore blobStore = blobStoreContext.getBlobStore(location.getId());
        blobStore.createContainerInLocation(getOnlyElement(blobStore.listAssignableLocations()), container);
    }

    public RegionScopedBlobStoreContext getBlobStoreContext() {
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

    public Location getLocation() {
        return location;
    }

    @Override
    public ImmutableBlobContainer immutableBlobContainer(BlobPath path) {
        return new CloudFilesImmutableBlobContainer(path, this);
    }

    @Override
    public void delete(BlobPath path) {
        blobStoreContext.getBlobStore(location.getId()).deleteDirectory(container, path.buildAsString("/"));
    }

    @Override
    public void close() {

    }

    @Override
    public String toString() {
        return container;
    }
}
