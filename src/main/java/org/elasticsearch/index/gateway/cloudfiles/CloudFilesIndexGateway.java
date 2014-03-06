package org.elasticsearch.index.gateway.cloudfiles;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.gateway.Gateway;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.gateway.IndexShardGateway;
import org.elasticsearch.index.gateway.blobstore.BlobStoreIndexGateway;
import org.elasticsearch.index.settings.IndexSettings;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:42 AM
 */
public class CloudFilesIndexGateway extends BlobStoreIndexGateway{
    @Inject
    protected CloudFilesIndexGateway(Index index, @IndexSettings Settings indexSettings, Gateway gateway) {
        super(index, indexSettings, gateway);
    }

    @Override
    public String type() {
        return "cloudfiles";
    }

    @Override
    public Class<? extends IndexShardGateway> shardGatewayClass() {
        return CloudFilesIndexShardGateway.class;
    }
}
