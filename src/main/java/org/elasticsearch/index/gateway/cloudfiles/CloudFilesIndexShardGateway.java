package org.elasticsearch.index.gateway.cloudfiles;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.gateway.IndexGateway;
import org.elasticsearch.index.gateway.blobstore.BlobStoreIndexShardGateway;
import org.elasticsearch.index.settings.IndexSettings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.shard.service.IndexShard;
import org.elasticsearch.index.store.Store;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:46 AM
 */
public class CloudFilesIndexShardGateway extends BlobStoreIndexShardGateway{
    @Inject
    protected CloudFilesIndexShardGateway(ShardId shardId, @IndexSettings Settings indexSettings, ThreadPool threadPool, IndexGateway indexGateway, IndexShard indexShard, Store store) {
        super(shardId, indexSettings, threadPool, indexGateway, indexShard, store);
    }

    @Override
    public String type() {
        return "cloudfiles";
    }
}
