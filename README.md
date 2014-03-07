Rackspace Cloud plugin for [Elasticsearch](http://www.elasticsearch.org/)
=========================================================================

This plugin enables the use of Rackspace Cloud Files for [snapshot / restore](http://www.elasticsearch.org/guide/en/elasticsearch/reference/master/modules-snapshots.html)
repositories and uses the Rackspace Cloud Servers API to perform automatic unicast host discovery.

## Installation

To install the plugin, run the following command:
```
bin/plugin --url https://github.com/jlinn/elasticsearch-cloud-rackspace/releases/download/v0.1.1/elasticsearch-cloud-rackspace-0.1.1.zip --install cloud-rackspace
```

| Rackspace Cloud Plugin | Elasticsearch |
|------------------------|---------------|
| 0.1.1 | 1.0.1 |

## Configuration

Since all snapshot and discovery operations are performed using Rackspace's API, credentials must be supplied in your Elasticsearch config as follows:
```
rackspace:
    account: your_account_name
    key: your_api_key
```

The region (datacenter) may be set using the `rackspace.region` config key.  Currently, only US regions (`DFW`, `ORD`, and `IAD`) are supported.

### Discovery

The following config parameter must be added in order to enable Cloud Servers discovery:
```
discovery.type: cloudServers
```

By default, the plugin uses only private IP addresses for discovery. If you wish to use public IP addresses, add the following setting:
```
discovery.cloudservers.private_address: false
```

### Cloud Files Repository

A Cloud Files repository can be created using the following command:
```
$ curl -XPUT 'http://localhost:9200/_snapshot/your_repository_name' -d '{
    "type": "cloudFiles",
    "settings": {
        "container": "my_container_name",
        "region": "ORD"
    }
}
```
If the specified container does not exist, it will automatically be created.
The following settings are supported:
* `container`: The name of the Cloud Files container to be used. This is mandatory.
* `region`: The datacenter to be used. Defaults to `ORD`. Currently, only `DFW`, `ORD`, and `IAD` are supported.
* `base_path`: Specifies the path within the container to store repository data. Defaults to the root of the container.
* `concurrent_streams`: Throttles the number of streams per node while performing a snapshot operation. Defaults to 5.
* `chunk_size`: Big files can be broken down into chunks during snapshotting if needed. The chunk size can be specified in bytes or by using size value notation, i.e. 1g, 10m, 5k. Defaults to 100m.
* `compress`: When set to true, metadata files are stored in compressed format. This setting doesn't affect index files which are already compressed by default. Defaults to false.
