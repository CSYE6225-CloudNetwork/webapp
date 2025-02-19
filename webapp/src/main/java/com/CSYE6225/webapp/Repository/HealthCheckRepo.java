package com.CSYE6225.webapp.Repository;
import com.CSYE6225.webapp.Entity.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthCheckRepo extends JpaRepository<HealthCheck,Long> {
}
