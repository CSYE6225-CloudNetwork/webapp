package com.CSYE6225.webapp.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="Profile")
public class Profile {
    @Id
    private String id;

    private String fileName;

    private String filePath;

    @Column(name = "uploadedDate", nullable = false)
    private LocalDateTime dateTime;

    public Profile() {
        this.id = UUID.randomUUID().toString();

    }

    @PrePersist
    public void prePersist() {
        if (this.dateTime == null) {
            this.dateTime = LocalDateTime.now(ZoneId.of("UTC"));
        } else {
            this.dateTime = this.dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        }
    }

}
