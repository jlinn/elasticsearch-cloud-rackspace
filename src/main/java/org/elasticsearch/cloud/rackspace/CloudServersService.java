package org.elasticsearch.cloud.rackspace;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 4:07 PM
 */
public class CloudServersService extends AbstractLifecycleComponent<CloudServersService>{
    private ComputeServiceContext context;

    @Inject
    protected CloudServersService(Settings settings, SettingsFilter settingsFilter) {
        super(settings);
        settingsFilter.addFilter(new RackspaceSettingsFilter());
    }

    public synchronized ComputeServiceContext context(){
        if(context != null){
            return context;
        }

        String account = componentSettings.get("account", settings.get("rackspace.account"));
        String key = componentSettings.get("key", settings.get("rackspace.key"));

        context = ContextBuilder.newBuilder("rackspace-cloudservers-us").credentials(account, key).buildView(ComputeServiceContext.class);
        return context;
    }

    @Override
    protected void doStart() throws ElasticsearchException {

    }

    @Override
    protected void doStop() throws ElasticsearchException {

    }

    @Override
    protected void doClose() throws ElasticsearchException {
        if(context != null){
            context.close();
        }
    }
}
