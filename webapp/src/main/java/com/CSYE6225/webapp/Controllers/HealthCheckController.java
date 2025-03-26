package com.CSYE6225.webapp.Controllers;

import com.CSYE6225.webapp.Services.HealthCheckService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;


@RestController
@RequestMapping("/healthz")
public class HealthCheckController {
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private HealthCheckService healthCheckService;

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @GetMapping("")
    public ResponseEntity<Void> getHealthCheckData(@RequestHeader HttpHeaders headers, HttpServletRequest request) {
        Instant start = Instant.now();
        Counter.builder("api.healthz.requests")
                .description("Health check API call count")
                .register(meterRegistry)
                .increment();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-Control", "no-cache, no-store, must-revalidate");
        responseHeaders.set("Pragma", "no-cache");
        responseHeaders.set("X-Content-Type-Options", "nosniff");

        if(request.getQueryString() != null || request.getContentLength() >0) {
            logger.warn("Request contains query parameters or body, returning 400 Bad Request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(responseHeaders).build();
        }
        try {
            logger.debug("Calling HealthCheckService to save health check data");
            healthCheckService.saveHealthCheck();
        }
        catch(Exception e)
        {
            logger.error("Database insertion failed", e);
            System.out.println("Insertion into Database Failed");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(responseHeaders).build();
        }
//        System.out.println(headers.get("Connection"));
// Return a 200 OK response
        Duration duration = Duration.between(start, Instant.now());
        Timer.builder("api.healthz.execution.time")
                .description("Time taken to process health check API request")
                .register(meterRegistry)
                .record(duration);
        logger.info("Health check passed, returning 200 OK");
       return ResponseEntity.ok().headers(responseHeaders).build();

    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH,RequestMethod.POST,RequestMethod.DELETE})
    public ResponseEntity<Void> methodNotAllowed() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-Control", "no-cache, no-store, must-revalidate");
        responseHeaders.set("Pragma", "no-cache");
        responseHeaders.set("X-Content-Type-Options", "nosniff");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(responseHeaders).build(); // 405 Method Not Allowed
    }
}
