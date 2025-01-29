package com.CSYE6225.webapp.Impl;

import com.CSYE6225.webapp.Entity.HealthCheck;
import com.CSYE6225.webapp.Services.HealthCheckService;
import com.CSYE6225.webapp.Repository.HealthCheckRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthCheckImpl implements HealthCheckService {

    @Autowired
    private HealthCheckRepo healthCheckRepository;

    public void saveHealthCheck()
    {
        HealthCheck healthCheck = new HealthCheck();
        healthCheckRepository.save(healthCheck);

    }
}
