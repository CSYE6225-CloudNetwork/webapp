package com.CSYE6225.webapp.Impl;

import com.CSYE6225.webapp.Entity.HealthCheck;
import com.CSYE6225.webapp.Services.HealthCheckService;
import com.CSYE6225.webapp.Repository.HealthCheckRepo;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Timer;


import java.time.LocalDateTime;

@Service
public class HealthCheckImpl implements HealthCheckService {

    @Autowired
    private HealthCheckRepo healthCheckRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    public void saveHealthCheck()
    {
        Timer.Sample healthzDBCall = Timer.start(meterRegistry);
        HealthCheck healthCheck = new HealthCheck();
        healthCheckRepository.save(healthCheck);
        healthzDBCall.stop(meterRegistry.timer("healthzDB.time"));

    }
}
