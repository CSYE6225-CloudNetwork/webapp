package com.CSYE6225.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthCheckImpl implements HealthCheckService{

    @Autowired
    private HealthCheckRepo healthCheckRepository;

    public void saveHealthCheck()
    {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setDateTime(LocalDateTime.now());
        healthCheckRepository.save(healthCheck);

    }
}
