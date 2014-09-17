package org.elasticsearch.repositories.cloudfiles;

import org.elasticsearch.cloud.rackspace.CloudFilesService;
import org.elasticsearch.cloud.rackspace.blobstore.CloudFilesBlobStore;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.snapshots.IndexShardRepository;
import org.elasticsearch.index.snapshots.blobstore.BlobStoreIndexShardRepository;
import org.elasticsearch.repositories.RepositoryException;
import org.elasticsearch.repositories.RepositorySettings;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudFilesRepositoryTest {
    protected IndexShardRepository indexShardRepository;

    protected CloudFilesService cloudFilesService;

    @Before
    public void setUp(){
        indexShardRepository = mock(BlobStoreIndexShardRepository.class);
        cloudFilesService = mock(CloudFilesService.class);

        RegionScopedBlobStoreContext blobStoreContext = mock(RegionScopedBlobStoreContext.class);

        BlobStore blobStore = mock(BlobStore.class);
        Set<Location> locations = new HashSet<Location>();
        Set<String> isoCodes = new HashSet<String>();
        isoCodes.add("US-IL");
        locations.add(new LocationBuilder().id("ORD").description("ORD").scope(LocationScope.REGION).iso3166Codes(isoCodes).build());
        when(blobStore.listAssignableLocations()).thenReturn((Set) locations);

        when(blobStoreContext.getBlobStore(anyString())).thenReturn(blobStore);

        when(cloudFilesService.context()).thenReturn(blobStoreContext);
    }

    @Test(expected = RepositoryException.class)
    public void testConstructorNoContainer(){
        new CloudFilesRepository("test", new RepositorySettings(ImmutableSettings.EMPTY, ImmutableSettings.EMPTY), indexShardRepository, cloudFilesService);
    }

    @Test
    public void testConstructor(){
        String name = "testRepo";
        String region = "DFW";
        String container = "testContainer";

        // Omit the region setting. Should default to "ORD"
        Settings settings = ImmutableSettings.builder()
                .put("container", container)
                .build();

        CloudFilesRepository repository = new CloudFilesRepository(name, new RepositorySettings(ImmutableSettings.EMPTY, settings), indexShardRepository, cloudFilesService);
        assertEquals("ORD", ((CloudFilesBlobStore) repository.blobStore()).getLocation().getId());

        // include the region setting this time
        settings = ImmutableSettings.builder()
                .put("container", container)
                .put("region", region)
                .build();

        repository = new CloudFilesRepository(name, new RepositorySettings(ImmutableSettings.EMPTY, settings), indexShardRepository, cloudFilesService);
        assertEquals(region, ((CloudFilesBlobStore) repository.blobStore()).getLocation().getId());

        Settings globalSettings = ImmutableSettings.builder()
                .put("repositories.cloudfiles.region", "IAD")
                .build();
        settings = ImmutableSettings.builder()
                .put("container", container)
                .build();

        repository = new CloudFilesRepository(name, new RepositorySettings(globalSettings, settings), indexShardRepository, cloudFilesService);
        // the region setting from the global settings should be used
        assertEquals("IAD", ((CloudFilesBlobStore) repository.blobStore()).getLocation().getId());

        globalSettings = ImmutableSettings.builder()
                .put("repositories.cloudfiles.region", "IAD")
                .build();
        settings = ImmutableSettings.builder()
                .put("container", container)
                .put("region", "DFW")
                .build();

        repository = new CloudFilesRepository(name, new RepositorySettings(globalSettings, settings), indexShardRepository, cloudFilesService);
        // the region setting from the global settings should be overridden
        assertEquals("DFW", ((CloudFilesBlobStore) repository.blobStore()).getLocation().getId());
    }
}