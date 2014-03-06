package org.elasticsearch.gateway.rackspace;

import org.elasticsearch.cloud.rackspace.CloudFilesService;
import org.elasticsearch.cloud.rackspace.blobstore.CloudFilesBlobStore;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.gateway.blobstore.BlobStoreGateway;
import org.elasticsearch.index.gateway.cloudfiles.CloudFilesIndexGatewayModule;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: Joe Linn
 * Date: 2/12/14
 * Time: 3:41 PM
 */
public class CloudFilesGateway extends BlobStoreGateway{
    private final ExecutorService concurrentStreamPool;

    @Inject
    protected CloudFilesGateway(Settings settings, ThreadPool threadPool, ClusterService clusterService, ClusterName clusterName, CloudFilesService cloudFilesService) throws IOException {
        super(settings, threadPool, clusterService);

        /*String username = componentSettings.get("username");
        if(username == null){
            throw new ElasticsearchIllegalArgumentException("No username defined for rackspace gateway.");
        }

        String apiKey = componentSettings.get("apiKey");
        if(apiKey == null){
            throw new ElasticsearchIllegalArgumentException("No apiKey defined for rackspace gateway.");
        }*/

        String container = componentSettings.get("container");
        if(container == null){
            throw new ElasticsearchIllegalArgumentException("No container defined for Rackspace cloud files gateway.");
        }

        String region = componentSettings.get("region");
        if(region == null){
            region = "cloudfiles-us";
        }
        if(!region.equals("cloudfiles-us")){
            //TODO: support other regions
            throw new ElasticsearchIllegalArgumentException(String.format("Region '%s' is not supported.", region));
        }

        ByteSizeValue chunkSize = componentSettings.getAsBytesSize("chunk_size", new ByteSizeValue(100, ByteSizeUnit.MB));

        int concurrentStreams = componentSettings.getAsInt("concurrent_streams", 5);
        this.concurrentStreamPool = EsExecutors.newScaling(1, concurrentStreams, 5, TimeUnit.SECONDS, EsExecutors.daemonThreadFactory(settings, "[cloudfiles_stream]"));

        logger.debug("using container [{}], region [{}], chunk_size [{}]", container, region, chunkSize);

        initialize(new CloudFilesBlobStore(settings, cloudFilesService.context(), container, cloudFilesService.location(), concurrentStreamPool), clusterName, chunkSize);
    }

    @Override
    protected void doClose() throws ElasticsearchException {
        super.doClose();
        concurrentStreamPool.shutdown();
    }

    @Override
    public String type() {
        return "cloudfiles";
    }

    @Override
    public Class<? extends Module> suggestIndexGateway() {
        return CloudFilesIndexGatewayModule.class;
    }
}
