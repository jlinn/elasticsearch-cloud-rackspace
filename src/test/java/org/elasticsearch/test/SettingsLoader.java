package org.elasticsearch.test;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.loader.YamlSettingsLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * User: Joe Linn
 * Date: 3/5/14
 * Time: 10:50 AM
 */
abstract public class SettingsLoader {
    public static Settings getSettingsFromResource(String path) throws IOException {
        InputStream is = SettingsLoader.class.getResourceAsStream(path);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder out = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            out.append("\n").append(line);
        }
        reader.close();
        Map<String,String> settingsMap = new YamlSettingsLoader().load(out.toString());

        return ImmutableSettings.settingsBuilder().put(settingsMap).build();
    }
}
