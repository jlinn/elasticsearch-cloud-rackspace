package org.elasticsearch.discovery.cloudservers;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.discovery.zen.ping.unicast.UnicastHostsProvider;
import org.elasticsearch.discovery.zen.ping.unicast.UnicastZenPing;
import org.elasticsearch.transport.TransportService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static org.jclouds.compute.predicates.NodePredicates.parentLocationId;
import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;

/**
 * User: Joe Linn
 * Date: 3/4/14
 * Time: 4:35 PM
 */
public class CloudServersUnicastHostsProvider extends AbstractComponent implements UnicastHostsProvider{
    private final TransportService transportService;

    private final ComputeServiceContext context;

    private final boolean bindAnyGroup;

    private final boolean privateIp;

    private final ImmutableSet<String> groups;

    private final ImmutableSet<String> tags;

    private final String region;

    @Inject
    public CloudServersUnicastHostsProvider(Settings settings, TransportService transportService, ComputeServiceContext context) {
        super(settings);
        this.transportService = transportService;
        this.context = context;

        this.bindAnyGroup = componentSettings.getAsBoolean("any_group", true);
        this.groups = ImmutableSet.copyOf(componentSettings.getAsArray("groups"));

        this.tags = ImmutableSet.copyOf(componentSettings.getAsArray("tags"));

        this.region = settings.get("rackspace.region", "ORD");

        this.privateIp = componentSettings.getAsBoolean("private_address", true);

        logger.debug("using tags [{}], groups [{}] with any_group [{}]", tags, groups, bindAnyGroup);
    }

    @Override
    public List<DiscoveryNode> buildDynamicNodes() {
        List<DiscoveryNode> discoveryNodes = Lists.newArrayList();
        String addressContains = componentSettings.get("address_contains", null);
        @SuppressWarnings("unchecked")
        Set<? extends ComputeMetadata> nodes = context.getComputeService().listNodesDetailsMatching(buildFilter());

        for(ComputeMetadata metadata : nodes){
            if(metadata instanceof NodeMetadata){
                if(tags.size() > 0){
                    // filtering by tags
                    boolean hasTag = false;
                    for(String tag : metadata.getTags()){
                        if(tags.contains(tag)){
                            hasTag = true;
                            break;
                        }
                    }
                    if(!hasTag){
                        // this node does not have any of the tags we are looking for
                        continue;
                    }
                }

                Set<String> nodeAddresses;
                if(privateIp){
                    nodeAddresses = ((NodeMetadata) metadata).getPrivateAddresses();
                }
                else{
                    nodeAddresses = ((NodeMetadata) metadata).getPublicAddresses();
                }
                String address = null;
                for(String ip : nodeAddresses){
                    if(addressContains != null && ip.contains(addressContains)){
                        address = ip;
                        break;
                    }
                    else if(addressContains == null){
                        address = ip;
                        break;
                    }
                }
                if(address != null){
                    try {
                        TransportAddress[] addresses = transportService.addressesFromString(address);
                        for(int i = 0; (i < addresses.length && i < UnicastZenPing.LIMIT_PORTS_COUNT); i++){
                            logger.trace("adding {}, address {}, transport_address {}", ((NodeMetadata) metadata).getHostname(), address, addresses[i]);
                            discoveryNodes.add(new DiscoveryNode("#cloud-" + ((NodeMetadata) metadata).getHostname() + "-" + i, addresses[i], Version.CURRENT));
                        }
                    } catch (Exception e) {
                        logger.warn("failed to add {}, address {}", e, ((NodeMetadata) metadata).getHostname(), address);
                    }
                }
                else{
                    logger.trace("not adding {}, address is null, host_type {}", ((NodeMetadata) metadata).getHostname());
                }
            }
        }

        logger.debug("using dynamic discovery nodes {}", discoveryNodes);

        return discoveryNodes;
    }

    public Predicate buildFilter(){
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(parentLocationId(region));
        if(this.groups.size() > 0){
            // filter based on group(s)
            List<Predicate> groups = new ArrayList<Predicate>();
            for(String group : this.groups){
                groups.add(runningInGroup(group));
            }
            if(bindAnyGroup){
                predicates.add(or(groups.toArray(new Predicate[groups.size()])));
            }
            else{
                predicates.add(and(groups.toArray(new Predicate[groups.size()])));
            }
        }
        return and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
