package com.CSYE6225.webapp.Config;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CloudWatchMetricsConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        StatsdConfig config = new StatsdConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String prefix() {
                return "csye6225";
            }
        };

        return new StatsdMeterRegistry(config, io.micrometer.core.instrument.Clock.SYSTEM);
    }
}