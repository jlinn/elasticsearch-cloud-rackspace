package org.elasticsearch.cloud.rackspace;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.SettingsFilter;

/**
 * User: Joe Linn
 * Date: 3/3/14
 * Time: 5:30 PM
 */
public class RackspaceSettingsFilter implements SettingsFilter.Filter{
    @Override
    public void filter(ImmutableSettings.Builder settings) {
        settings.remove("rackspace.account");
        settings.remove("rackspace.key");
    }
}
