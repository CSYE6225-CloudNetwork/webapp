package com.CSYE6225.webapp.Services;

import org.springframework.stereotype.Service;

@Service
public interface HealthCheckService {



//    @Autowired
//    private HealthCheckImpl healthCheckImpl;

    public default void saveHealthCheck() {}
}
