package org.elasticsearch.discovery.cloudservers;

import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.zen.ZenDiscoveryModule;

/**
 * User: Joe Linn
 * Date: 3/5/14
 * Time: 12:18 PM
 */
public class CloudServersDiscoveryModule extends ZenDiscoveryModule{
    @Override
    protected void bindDiscovery() {
        bind(Discovery.class).to(CloudServersDiscovery.class).asEagerSingleton();
    }
}
