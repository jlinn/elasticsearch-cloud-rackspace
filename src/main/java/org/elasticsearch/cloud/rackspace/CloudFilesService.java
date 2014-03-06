package org.elasticsearch.cloud.rackspace;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.domain.Location;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:09 AM
 */
public class CloudFilesService extends AbstractLifecycleComponent<CloudFilesService>{
    private BlobStoreContext context;

    private Location location;

    @Inject
    protected CloudFilesService(Settings settings, SettingsFilter settingsFilter) {
        super(settings);
        settingsFilter.addFilter(new RackspaceSettingsFilter());
    }

    public synchronized Location location(){
        if(location != null){
            return location;
        }

        String dataCenter = componentSettings.get("region", "ORD");

        location = new LocationBuilder().scope(LocationScope.REGION).id(dataCenter).description("A Rackspace data center.").build();

        return location;
    }

    public synchronized BlobStoreContext context(){
        if(context != null){
            return context;
        }

        String account = settings.get("rackspace.account");
        String key = settings.get("rackspace.key");

        context = ContextBuilder.newBuilder("cloudfiles-us").credentials(account, key).buildView(BlobStoreContext.class);

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
