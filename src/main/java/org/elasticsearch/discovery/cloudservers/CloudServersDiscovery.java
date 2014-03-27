package org.elasticsearch.discovery.cloudservers;

import org.elasticsearch.Version;
import org.elasticsearch.cloud.rackspace.CloudServersService;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNodeService;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.DiscoverySettings;
import org.elasticsearch.discovery.zen.ZenDiscovery;
import org.elasticsearch.discovery.zen.ping.ZenPing;
import org.elasticsearch.discovery.zen.ping.ZenPingService;
import org.elasticsearch.discovery.zen.ping.unicast.UnicastZenPing;
import org.elasticsearch.node.settings.NodeSettingsService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * User: Joe Linn
 * Date: 3/5/14
 * Time: 12:12 PM
 */
public class CloudServersDiscovery extends ZenDiscovery{
    @Inject
    public CloudServersDiscovery(Settings settings, ClusterName clusterName, ThreadPool threadPool, TransportService transportService,
                                 ClusterService clusterService, NodeSettingsService nodeSettingsService, DiscoveryNodeService discoveryNodeService,
                                 ZenPingService pingService, Version version, DiscoverySettings discoverySettings, CloudServersService cloudServersService) {
        super(settings, clusterName, threadPool, transportService, clusterService, nodeSettingsService, discoveryNodeService, pingService, version, discoverySettings);
        if(settings.getAsBoolean("rackspace.enabled", true)){
            ImmutableList<? extends ZenPing> zenPings = pingService.zenPings();
            UnicastZenPing unicastZenPing = null;
            for(ZenPing zenPing : zenPings){
                if(zenPing instanceof UnicastZenPing){
                    unicastZenPing = (UnicastZenPing) zenPing;
                    break;
                }
            }

            if(unicastZenPing != null){
                unicastZenPing.addHostsProvider(new CloudServersUnicastHostsProvider(settings, transportService, cloudServersService.context()));
                pingService.zenPings(ImmutableList.<ZenPing>of(unicastZenPing));
            }
            else{
                logger.warn("failed to apply cloud servers unicast discovery. No unicast ping found.");
            }
        }
    }
}
