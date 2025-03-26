package com.CSYE6225.webapp.Impl;

import com.CSYE6225.webapp.Entity.HealthCheck;
import com.CSYE6225.webapp.Services.HealthCheckService;
import com.CSYE6225.webapp.Repository.HealthCheckRepo;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;

import java.time.LocalDateTime;

@Service
public class HealthCheckImpl implements HealthCheckService {

    @Autowired
    private HealthCheckRepo healthCheckRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    public void saveHealthCheck()
    {
        Instant start = Instant.now();
        HealthCheck healthCheck = new HealthCheck();
        healthCheckRepository.save(healthCheck);

        Duration duration = Duration.between(start, Instant.now());
        Timer.builder("database.healthcheck.insert.time")
                .description("Time taken to insert health check data into DB")
                .register(meterRegistry)
                .record(duration);

    }
}
