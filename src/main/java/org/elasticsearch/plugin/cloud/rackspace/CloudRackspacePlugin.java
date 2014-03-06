package org.elasticsearch.plugin.cloud.rackspace;

import org.elasticsearch.cloud.rackspace.CloudFilesService;
import org.elasticsearch.cloud.rackspace.CloudServersService;
import org.elasticsearch.cloud.rackspace.RackspaceModule;
import org.elasticsearch.repositories.cloudfiles.CloudFilesRepository;
import org.elasticsearch.repositories.cloudfiles.CloudFilesRepositoryModule;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.repositories.RepositoriesModule;

import java.util.Collection;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 10:51 AM
 */
public class CloudRackspacePlugin extends AbstractPlugin{
    private final Settings settings;

    public CloudRackspacePlugin(Settings settings){
        this.settings = settings;
    }

    @Override
    public String name() {
        return "cloud-rackspace";
    }

    @Override
    public String description() {
        return "Rackspace Cloud Plugin";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = Lists.newArrayList();
        if(settings.getAsBoolean("rackspace.enabled", true)){
            modules.add(RackspaceModule.class);
        }
        return modules;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = Lists.newArrayList();
        if(settings.getAsBoolean("rackspace.enabled", true)){
            services.add(CloudFilesService.class);
            services.add(CloudServersService.class);
        }
        return services;
    }

    public void onModule(RepositoriesModule repositoriesModule){
        if(settings.getAsBoolean("rackspace.enabled", true)){
            repositoriesModule.registerRepository(CloudFilesRepository.TYPE, CloudFilesRepositoryModule.class);
        }
    }
}
