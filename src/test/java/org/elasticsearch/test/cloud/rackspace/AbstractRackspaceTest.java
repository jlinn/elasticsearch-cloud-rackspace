package org.elasticsearch.test.cloud.rackspace;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;
import org.elasticsearch.test.ElasticsearchIntegrationTest;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 11:45 AM
 */
public class AbstractRackspaceTest extends ElasticsearchIntegrationTest{
    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @TestGroup(enabled = false, sysProperty = SYSPROP_RACKSPACE)
    public @interface RackspaceTest{

    }

    public static final String SYSPROP_RACKSPACE = "tests.rackspace";
}
