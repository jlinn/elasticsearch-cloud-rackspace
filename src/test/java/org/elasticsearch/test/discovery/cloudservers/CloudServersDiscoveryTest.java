package org.elasticsearch.test.discovery.cloudservers;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.cloud.rackspace.AbstractRackspaceTest;
import org.junit.Test;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

/**
 * User: Joe Linn
 * Date: 3/5/14
 * Time: 2:26 PM
 */
@AbstractRackspaceTest.RackspaceTest
@ElasticsearchIntegrationTest.ClusterScope(scope = ElasticsearchIntegrationTest.Scope.TEST, numDataNodes = 0, numClientNodes = 0)
public class CloudServersDiscoveryTest extends AbstractRackspaceTest{
    @Test
    public void testStart(){
        Settings nodeSettings = settingsBuilder().put("rackspace.enabled", true)
                .put("discovery.type", "cloudServers")
                .put("node.client", true)
                .build();
        cluster().startNode(nodeSettings);
    }
}
