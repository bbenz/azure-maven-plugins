/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.maven.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import org.codehaus.plexus.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class AppInsightsProxy implements TelemetryProxy {
    protected TelemetryClient client;

    protected TelemetryConfiguration configuration;

    protected Map<String, String> defaultProperties;

    // Telemetry is enabled by default.
    protected boolean isEnabled = true;

    public AppInsightsProxy(final TelemetryConfiguration config) {
        // InstrumentationKey will be read from ApplicationInsights.xml
        client = new TelemetryClient();
        if (config == null) {
            throw new NullPointerException();
        }
        configuration = config;
        defaultProperties = configuration.getTelemetryProperties();
    }

    public void addDefaultProperty(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        defaultProperties.put(key, value);
    }

    public Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    public void trackEvent(final String eventName) {
        trackEvent(eventName, null, false);
    }

    public void trackEvent(final String eventName, final Map<String, String> customProperties) {
        trackEvent(eventName, customProperties, false);
    }

    public void trackEvent(final String eventName, final Map<String, String> customProperties,
                           final boolean overrideDefaultProperties) {
        if (!isEnabled ) {
            return;
        }

        final Map<String, String> properties = mergeProperties(getDefaultProperties(), customProperties,
                overrideDefaultProperties);

        client.trackEvent(eventName, properties, null);
        client.flush();
    }

    protected Map<String, String> mergeProperties(Map<String, String> defaultProperties,
                                                  Map<String, String> customProperties,
                                                  boolean overrideDefaultProperties) {
        if (customProperties == null) {
            return defaultProperties;
        }

        final Map<String, String> merged = new HashMap<>();
        if (overrideDefaultProperties) {
            merged.putAll(defaultProperties);
            merged.putAll(customProperties);
        } else {
            merged.putAll(customProperties);
            merged.putAll(defaultProperties);
        }
        return merged;
    }
}
