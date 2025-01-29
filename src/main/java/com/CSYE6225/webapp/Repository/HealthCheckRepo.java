package com.CSYE6225.webapp;
import org.springframework.data.jpa.repository.JpaRepository;

interface HealthCheckRepo extends JpaRepository<HealthCheck,Long> {
}
