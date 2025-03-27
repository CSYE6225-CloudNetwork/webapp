package com.CSYE6225.webapp.Config;


import io.micrometer.core.instrument.Clock;
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
                return Duration.ofSeconds(60);
            }

            @Override
            public String prefix() {
                return "csye6225-webapp";
            }

            @Override
            public String host() {
                return "localhost";
            }

            @Override
            public int port() {
                return 8125;
            }
        };
        return new StatsdMeterRegistry(config, Clock.SYSTEM);
    }
}