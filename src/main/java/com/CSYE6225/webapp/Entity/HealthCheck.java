package com.CSYE6225.webapp.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(name="HealthCheck")
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use IDENTITY to avoid sequence table
    @Column(name = "checkID")
    private Long checkID;

    @Column(name="DateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private LocalDateTime dateTime;

    // Ensure dateTime is stored in UTC before persisting
    @PrePersist
    public void prePersist() {
        if (this.dateTime == null) {
            this.dateTime = LocalDateTime.now(ZoneId.of("UTC"));
        } else {
            this.dateTime = this.dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        }
    }
}
