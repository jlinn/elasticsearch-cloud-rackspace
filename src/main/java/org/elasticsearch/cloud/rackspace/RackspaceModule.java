package org.elasticsearch.cloud.rackspace;

import org.elasticsearch.cloud.rackspace.CloudFilesService;
import org.elasticsearch.common.inject.AbstractModule;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:31 AM
 */
public class RackspaceModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(CloudFilesService.class).asEagerSingleton();
        bind(CloudServersService.class).asEagerSingleton();
    }
}
