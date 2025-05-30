package com.CSYE6225.webapp.Controllers;

import com.CSYE6225.webapp.Entity.Profile;
import com.CSYE6225.webapp.Services.ProfileService;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.core.instrument.Timer;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/v2/file")
public class ProfileController {
    private final ProfileService profileService;
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Value("${S3.BucketName}") String bucketName;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("profilePic") MultipartFile file) {
        logger.info("Uploading profile picture started: {}", file.getOriginalFilename());
        meterRegistry.counter("profilePicture.save.count").increment();
        Timer.Sample apiCallTimer = Timer.start(meterRegistry);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Profile profile = profileService.saveProfile(file);
            apiCallTimer.stop(meterRegistry.timer("profilePicture.save"));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "file_name", profile.getFileName(),
                    "id", profile.getId(),
                    "url", bucketName+"/" + profile.getFilePath(),
                    "upload_date", profile.getDateTime().toString()
            ));
        } catch (IOException e) {
            logger.error("bad request: {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFileMetadata(@PathVariable String id) {
        meterRegistry.counter("profilePicture.count").increment();

        meterRegistry.counter("profilePicture.get.count").increment();
        Timer.Sample apiCallTimer = Timer.start(meterRegistry);
        logger.info("get profile picture started: {}", id);
        if (id == null || id.isEmpty()) {
            logger.info("invalid or empty id: {}",id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Profile profile = profileService.getProfilePicture(id);
        if (profile != null) {
            apiCallTimer.stop(meterRegistry.timer("profilePicture.get"));
            logger.info("get profile picture finished: {}", id);
            return ResponseEntity.ok(Map.of(
                    "file_name", profile.getFileName(),
                    "id", profile.getId(),
                    "url", bucketName+"/" + profile.getFilePath(),
                    "upload_date", profile.getDateTime().toString()
            ));
        } else {
            logger.info("profile Picture not found with id: {}",id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        meterRegistry.counter("profilePicture.delete.count").increment();

        meterRegistry.counter("delete.profilePicture.count").increment();
        Timer.Sample apiCallTimer = Timer.start(meterRegistry);
        logger.info("delete profile picture started: {}", id);
        if (id == null || id.isEmpty()) {
            logger.info("invalid or empty id for delete: {}",id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (profileService.deleteProfile(id)) {
            apiCallTimer.stop(meterRegistry.timer("profilePicture.delete"));
            logger.info("delete profile picture finished: {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        } else {
            logger.info("profile Picture not found for delete with id: {}",id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }
    }

    @GetMapping
    public ResponseEntity<Void> getFileWithoutId() {
        logger.warn("Attempt to get profile picture metadata without ID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFileWithoutId() {
        logger.warn("Attempt to delete profile picture without ID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowedFile() {
        logger.warn("Unsupported HTTP method attempted on /v1/file endpoint");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build(); // 405 Method Not Allowed
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowedFileId() {
        logger.warn("Unsupported HTTP method attempted on /v1/file/{id} endpoint");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build(); // 405 Method Not Allowed
    }
}
