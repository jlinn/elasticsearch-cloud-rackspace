package org.elasticsearch.test.discovery.cloudservers;

import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.cloudservers.CloudServersUnicastHostsProvider;
import org.elasticsearch.test.SettingsLoader;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.transport.local.LocalTransport;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * User: Joe Linn
 * Date: 3/5/14
 * Time: 10:42 AM
 */
public class CloudServersUnicastHostsProviderTest {
    protected Settings settings;

    protected TransportService transportService;

    protected ComputeServiceContext context;

    @Before
    public void setUp() throws IOException {
        settings = SettingsLoader.getSettingsFromResource("/elasticsearch.yml");
        ThreadPool threadPool = new ThreadPool("testThreadPool");
        transportService = new TransportService(new LocalTransport(settings, threadPool, Version.fromId(1)), threadPool);

        String account = settings.get("rackspace.account");
        String key = settings.get("rackspace.key");
        context = ContextBuilder.newBuilder("rackspace-cloudservers-us").credentials(account, key).buildView(ComputeServiceContext.class);
    }

    @Test
    public void testBuildDynamicNodes(){
        CloudServersUnicastHostsProvider provider = new CloudServersUnicastHostsProvider(settings, transportService, context);
        provider.buildDynamicNodes();
    }
}
