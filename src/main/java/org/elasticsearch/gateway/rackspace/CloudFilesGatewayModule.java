package org.elasticsearch.gateway.rackspace;

import org.elasticsearch.gateway.Gateway;
import org.elasticsearch.gateway.blobstore.BlobStoreGatewayModule;
import org.elasticsearch.gateway.rackspace.CloudFilesGateway;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:41 AM
 */
public class CloudFilesGatewayModule extends BlobStoreGatewayModule{
    @Override
    protected void configure() {
        bind(Gateway.class).to(CloudFilesGateway.class).asEagerSingleton();
    }
}
