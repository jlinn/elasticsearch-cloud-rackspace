package org.elasticsearch.repositories.cloudfiles;

import org.elasticsearch.cloud.rackspace.CloudFilesService;
import org.elasticsearch.cloud.rackspace.blobstore.CloudFilesBlobStore;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStore;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.index.snapshots.IndexShardRepository;
import org.elasticsearch.repositories.RepositoryException;
import org.elasticsearch.repositories.RepositorySettings;
import org.elasticsearch.repositories.blobstore.BlobStoreRepository;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:58 AM
 */
public class CloudFilesRepository extends BlobStoreRepository{
    public final static String TYPE = "cloudfiles";

    private final CloudFilesBlobStore blobStore;

    private final BlobPath basePath;

    private ByteSizeValue chunkSize;

    private boolean compress;

    @Inject
    protected CloudFilesRepository(String repositoryName, RepositorySettings repositorySettings, IndexShardRepository indexShardRepository, CloudFilesService cloudFilesService) {
        super(repositoryName, repositorySettings, indexShardRepository);

        String container = repositorySettings.settings().get("container", componentSettings.get("container"));
        if(container == null){
            throw new RepositoryException(repositoryName, "No container defined for cloud files gateway.");
        }

        String dataCenter = repositorySettings.settings().get("dataCenter", componentSettings.get("dataCenter"));
        if(dataCenter == null){
            dataCenter = "ORD";
        }
        int concurrentStreams = repositorySettings.settings().getAsInt("concurrent_streams", componentSettings.getAsInt("concurrent_streams", 5));
        ExecutorService concurrentStreamPool = EsExecutors.newScaling(1, concurrentStreams, 5, TimeUnit.SECONDS, EsExecutors.daemonThreadFactory(settings, "[cloudfiles_stream]"));
        Location location = new LocationBuilder().scope(LocationScope.REGION).id(dataCenter).description("Rackspace's ORD datacenter.").build();

        logger.debug("using container [{}], data center [{}], chunk_size [{}], concurrent_streams [{}]", container, dataCenter, chunkSize, concurrentStreams);
        blobStore = new CloudFilesBlobStore(settings, cloudFilesService.context(), container, location, concurrentStreamPool);
        this.chunkSize = repositorySettings.settings().getAsBytesSize("chunk_size", componentSettings.getAsBytesSize("chunk_size", new ByteSizeValue(100, ByteSizeUnit.MB)));
        this.compress = repositorySettings.settings().getAsBoolean("compress", componentSettings.getAsBoolean("compress", false));
        String basePath = repositorySettings.settings().get("base_path", null);
        if (Strings.hasLength(basePath)) {
            BlobPath path = new BlobPath();
            for(String elem : Strings.splitStringToArray(basePath, '/')) {
                path = path.add(elem);
            }
            this.basePath = path;
        } else {
            this.basePath = BlobPath.cleanPath();
        }
    }

    @Override
    protected BlobStore blobStore() {
        return blobStore;
    }

    @Override
    protected BlobPath basePath() {
        return basePath;
    }

    @Override
    protected boolean isCompress() {
        return compress;
    }

    @Override
    protected ByteSizeValue chunkSize() {
        return chunkSize;
    }
}
