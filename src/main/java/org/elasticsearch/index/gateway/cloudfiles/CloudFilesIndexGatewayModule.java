package org.elasticsearch.index.gateway.cloudfiles;

import org.elasticsearch.index.gateway.cloudfiles.CloudFilesIndexGateway;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.index.gateway.IndexGateway;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:49 AM
 */
public class CloudFilesIndexGatewayModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(IndexGateway.class).to(CloudFilesIndexGateway.class).asEagerSingleton();
    }
}
