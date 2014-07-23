package org.elasticsearch.test.repositories.cloudfiles;

import org.elasticsearch.cloud.rackspace.CloudFilesService;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.snapshots.SnapshotInfo;
import org.elasticsearch.test.cloud.rackspace.AbstractRackspaceTest;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryResponse;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.math.UnboxedMathUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.snapshots.SnapshotState;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.store.MockDirectoryHelper;
import org.hamcrest.Matchers;
import org.jclouds.blobstore.BlobStoreContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 11:43 AM
 */
@AbstractRackspaceTest.RackspaceTest
@ElasticsearchIntegrationTest.ClusterScope(scope = ElasticsearchIntegrationTest.Scope.TEST, numDataNodes = 2)
public class CloudFilesSnapshotRestoreTest extends AbstractRackspaceTest{
    public static final String DEFAULT_CONTAINER = "es_snapshot_test";

    @Override
    public Settings indexSettings() {
        return ImmutableSettings.builder().put(super.indexSettings())
                .put(MockDirectoryHelper.RANDOM_PREVENT_DOUBLE_WRITE, false)
                .put(MockDirectoryHelper.RANDOM_NO_DELETE_OPEN_FILE, false)
                .put("rackspace.enabled", true)
                .put("repositories.cloudfiles.container", DEFAULT_CONTAINER)
                .put("cloudfiles.container", DEFAULT_CONTAINER)
                .build();
    }

    private String basePath;

    @Before
    public final void wipeBefore(){
        deleteRepository();
        basePath = "repo-" + UnboxedMathUtils.randomInt();
        cleanRepositoryFiles(basePath);
    }

    @After
    public final void wipeAfter(){
        //cleanRepositoryFiles(basePath);
        deleteRepository();
    }

    @Test
    public void testSimpleWorkflow(){
        Client client = client();
        PutRepositoryResponse putRepositoryResponse = client.admin().cluster().preparePutRepository("test-repo")
                .setType("cloudfiles").setSettings(ImmutableSettings.settingsBuilder()
                    .put("base_path", basePath)
                    .put("chunk_size", randomIntBetween(1000, 10000))
                ).get();
        assertThat(putRepositoryResponse.isAcknowledged(), equalTo(true));

        createIndex("test-idx-1", "test-idx-2", "test-idx-3");
        ensureGreen();

        logger.info("--> indexing some data");
        for (int i = 0; i < 100; i++) {
            index("test-idx-1", "doc", Integer.toString(i), "foo", "bar" + i);
            index("test-idx-2", "doc", Integer.toString(i), "foo", "baz" + i);
            index("test-idx-3", "doc", Integer.toString(i), "foo", "baz" + i);
        }
        refresh();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-2").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-3").get().getCount(), equalTo(100L));

        logger.info("--> snapshot");
        CreateSnapshotResponse createSnapshotResponse = client.admin().cluster().prepareCreateSnapshot("test-repo", "test-snap")
                .setWaitForCompletion(true).setIndices("test-idx-*", "-test-idx-3").get();
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), greaterThan(0));
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), Matchers.equalTo(createSnapshotResponse.getSnapshotInfo().totalShards()));

        assertThat(client.admin().cluster().prepareGetSnapshots("test-repo").setSnapshots("test-snap").get().getSnapshots().get(0).state(), Matchers.equalTo(SnapshotState.SUCCESS));

        ImmutableList<SnapshotInfo> snapshots = client.admin().cluster().prepareGetSnapshots("test-repo").get().getSnapshots();
        assertThat(snapshots, hasSize(1));

        logger.info("--> delete some data");
        for (int i = 0; i < 50; i++) {
            client.prepareDelete("test-idx-1", "doc", Integer.toString(i)).get();
        }
        for (int i = 50; i < 100; i++) {
            client.prepareDelete("test-idx-2", "doc", Integer.toString(i)).get();
        }
        for (int i = 0; i < 100; i += 2) {
            client.prepareDelete("test-idx-3", "doc", Integer.toString(i)).get();
        }
        refresh();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(50L));
        assertThat(client.prepareCount("test-idx-2").get().getCount(), equalTo(50L));
        assertThat(client.prepareCount("test-idx-3").get().getCount(), equalTo(50L));

        logger.info("--> close indices");
        client.admin().indices().prepareClose("test-idx-1", "test-idx-2").get();

        logger.info("--> restore all indices from the snapshot");
        RestoreSnapshotResponse restoreSnapshotResponse = client.admin().cluster().prepareRestoreSnapshot("test-repo", "test-snap").setWaitForCompletion(true).execute().actionGet();
        assertThat(restoreSnapshotResponse.getRestoreInfo().totalShards(), greaterThan(0));

        ensureGreen();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-2").get().getCount(), equalTo(100L));
        assertThat(client.prepareCount("test-idx-3").get().getCount(), equalTo(50L));

        // Test restore after index deletion
        logger.info("--> delete indices");
        client.admin().indices().prepareDelete("test-idx-1", "test-idx-2").get();
        logger.info("--> restore one index after deletion");
        restoreSnapshotResponse = client.admin().cluster().prepareRestoreSnapshot("test-repo", "test-snap").setWaitForCompletion(true).setIndices("test-idx-*", "-test-idx-2").execute().actionGet();
        assertThat(restoreSnapshotResponse.getRestoreInfo().totalShards(), greaterThan(0));
        ensureGreen();
        assertThat(client.prepareCount("test-idx-1").get().getCount(), equalTo(100L));
        ClusterState clusterState = client.admin().cluster().prepareState().get().getState();
        assertThat(clusterState.getMetaData().hasIndex("test-idx-1"), equalTo(true));
        assertThat(clusterState.getMetaData().hasIndex("test-idx-2"), equalTo(false));
    }

    public void cleanRepositoryFiles(String basePath){
        String container = internalCluster().getInstance(Settings.class).get("repositories.cloudfiles.container", DEFAULT_CONTAINER);
        BlobStoreContext context = internalCluster().getInstance(CloudFilesService.class).context();
        context.getBlobStore().deleteDirectory(container, basePath);
    }

    public void deleteRepository(){
        String container = internalCluster().getInstance(Settings.class).get("repositories.cloudfiles.container", DEFAULT_CONTAINER);
        internalCluster().getInstance(CloudFilesService.class).context().getBlobStore().deleteContainer(container);
    }
}
