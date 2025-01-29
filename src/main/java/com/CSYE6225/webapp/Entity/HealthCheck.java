package com.CSYE6225.webapp;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Table(name="HealthCheck")
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use IDENTITY to avoid sequence table
    @Column(name = "checkID")
    private Long checkID;

    @Column(name="DateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime dateTime;


    public void setDateTime(LocalDateTime now) {
        this.dateTime = now;
    }
}
