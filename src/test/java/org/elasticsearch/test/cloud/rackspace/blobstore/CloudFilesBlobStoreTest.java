package org.elasticsearch.test.cloud.rackspace.blobstore;

import junit.framework.TestCase;
import org.elasticsearch.cloud.rackspace.blobstore.CloudFilesBlobStore;
import org.elasticsearch.common.util.concurrent.MoreExecutors;
import org.elasticsearch.test.cloud.rackspace.blobstore.AbstractBlobStoreTest;
import org.junit.Test;

/**
 * User: Joe Linn
 * Date: 3/3/14
 * Time: 12:28 PM
 */
public class CloudFilesBlobStoreTest extends AbstractBlobStoreTest {
    @Test
    public void testConstructor(){
        String containerName = "constructor_test_container";
        containers.add(containerName);

        new CloudFilesBlobStore(settings, blobStoreContext, containerName, location, MoreExecutors.sameThreadExecutor());

        // the container should have been created by the constructor
        TestCase.assertTrue(blobStoreContext.getBlobStore().containerExists(containerName));
    }
}
