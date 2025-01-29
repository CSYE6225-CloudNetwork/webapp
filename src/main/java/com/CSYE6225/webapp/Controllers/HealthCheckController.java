package com.CSYE6225.webapp;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/healthz")
public class HealthCheckController {

    @Autowired
    private HealthCheckService healthCheckService;

    @GetMapping("")
    public ResponseEntity<Void> getHealthCheckData(@RequestHeader HttpHeaders headers, HttpServletRequest request) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-Control", "no-cache, no-store, must-revalidate");
        responseHeaders.set("Pragma", "no-cache");
        responseHeaders.set("X-Content-Type-Options", "nosniff");

        if(request.getQueryString() != null || request.getContentLength() >0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(responseHeaders).build();
        }
        try {
            healthCheckService.saveHealthCheck();
        }
        catch(Exception e)
        {
            System.out.println("Insertion into Database Failed");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(responseHeaders).build();
        }
//        System.out.println(headers.get("Connection"));
// Return a 200 OK response
       return ResponseEntity.ok().headers(responseHeaders).build();

    }
}
