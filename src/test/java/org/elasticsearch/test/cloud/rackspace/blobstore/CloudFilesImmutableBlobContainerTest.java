package org.elasticsearch.test.cloud.rackspace.blobstore;

import junit.framework.TestCase;
import org.elasticsearch.cloud.rackspace.blobstore.CloudFilesBlobStore;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.elasticsearch.common.util.concurrent.MoreExecutors;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * User: Joe Linn
 * Date: 3/3/14
 * Time: 4:03 PM
 */
public class CloudFilesImmutableBlobContainerTest extends AbstractBlobStoreTest {
    @Test
    public void testBlobContainerWrite() throws IOException, InterruptedException {
        String containerName = "write_test_container";
        containers.add(containerName);

        // delete the test container if it already exists
        if(blobStoreContext.getBlobStore().containerExists(containerName)){
            blobStoreContext.getBlobStore().deleteContainer(containerName);
        }

        CloudFilesBlobStore blobStore = getBlobStore(containerName);
        String blobPath = "foo/bar/";
        String blobName = "testBlob.txt";
        String input = "this is a test";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"));
        blobStore.immutableBlobContainer(new BlobPath().add(blobPath)).writeBlob(blobName, inputStream, (long) input.getBytes("UTF-8").length);

        //Thread.sleep(1000L);

        // ensure that the "folders" and file have been created properly
        String path = "";
        String separator = "";
        for(String pathPart : (blobPath + blobName).split("/")){
            path += separator + pathPart;
            TestCase.assertTrue(String.format("Path %s does not exist.", path), blobStoreContext.getBlobStore().blobExists(containerName, path));
            separator = "/";
        }
    }

    @Test
    public void testBlobExists() throws UnsupportedEncodingException {
        String containerName = "exists_test_container";
        containers.add(containerName);

        CloudFilesBlobStore blobStore = getBlobStore(containerName);
        ImmutableBlobContainer immutableBlobContainer = blobStore.immutableBlobContainer(new BlobPath());

        String fileName = "test.txt";
        ByteArrayInputStream inputStream = new ByteArrayInputStream("foobar".getBytes("UTF-8"));
        blobStoreContext.getBlobStore().putBlob(containerName, blobStoreContext.getBlobStore().blobBuilder(fileName).payload(inputStream).build());

        TestCase.assertTrue(immutableBlobContainer.blobExists(fileName));
        TestCase.assertFalse(immutableBlobContainer.blobExists("bobaweifewf"));
    }

    @Test
    public void testListBlobs() throws IOException {
        String containerName = "list_test_container";
        containers.add(containerName);

        CloudFilesBlobStore blobStore = getBlobStore(containerName);
        ImmutableBlobContainer immutableBlobContainer = blobStore.immutableBlobContainer(new BlobPath());

        // create test files
        String[] fileNames = new String[]{"test.txt", "foo.txt", "bar.txt", "bob.txt"};
        for(String fileName : fileNames){
            blobStoreContext.getBlobStore().putBlob(containerName, blobStoreContext.getBlobStore().blobBuilder(fileName)
                    .payload(new ByteArrayInputStream("foobar".getBytes("UTF-8"))).build());
        }

        immutableBlobContainer.listBlobs();
    }

    protected CloudFilesBlobStore getBlobStore(String container){
        return new CloudFilesBlobStore(settings, blobStoreContext, container, location, MoreExecutors.sameThreadExecutor());
    }
}
