package org.elasticsearch.test.cloud.rackspace.blobstore;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.SettingsLoader;
import org.jclouds.ContextBuilder;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Joe Linn
 * Date: 3/3/14
 * Time: 4:02 PM
 */
abstract public class AbstractBlobStoreTest {
    protected Settings settings;

    protected RegionScopedBlobStoreContext blobStoreContext;

    protected org.jclouds.domain.Location location;

    protected List<String> containers = new ArrayList<String>();

    @Before
    public void setUp() throws IOException {
        settings = SettingsLoader.getSettingsFromResource("/elasticsearch.yml");

        String account = settings.get("rackspace.account");
        String key = settings.get("rackspace.key");
        blobStoreContext = ContextBuilder.newBuilder("rackspace-cloudfiles-us").credentials(account, key)
                .buildView(RegionScopedBlobStoreContext.class);
        location = new LocationBuilder().scope(LocationScope.REGION).id("ORD").description("ORD").build();
    }

    @After
    public void tearDown(){
        for(String name : containers){
            blobStoreContext.getBlobStore().deleteContainer(name);
        }
    }
}
