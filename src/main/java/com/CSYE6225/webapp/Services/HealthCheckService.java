package com.CSYE6225.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface HealthCheckService {



//    @Autowired
//    private HealthCheckImpl healthCheckImpl;

    public default void saveHealthCheck() {}
}
