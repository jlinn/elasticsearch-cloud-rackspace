package org.elasticsearch.repositories.cloudfiles;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.index.snapshots.IndexShardRepository;
import org.elasticsearch.index.snapshots.blobstore.BlobStoreIndexShardRepository;
import org.elasticsearch.repositories.Repository;
import org.elasticsearch.repositories.cloudfiles.CloudFilesRepository;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 11:26 AM
 */
public class CloudFilesRepositoryModule extends AbstractModule{
    public CloudFilesRepositoryModule() {
        super();
    }

    @Override
    protected void configure() {
        bind(Repository.class).to(CloudFilesRepository.class).asEagerSingleton();
        bind(IndexShardRepository.class).to(BlobStoreIndexShardRepository.class).asEagerSingleton();
    }
}
