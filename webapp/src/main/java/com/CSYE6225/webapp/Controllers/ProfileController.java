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
import java.util.Map;

@RestController
@RequestMapping("/v1/file")
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

        if (file == null || file.isEmpty()) {
            logger.warn("Uploaded file is null or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Profile profile = profileService.saveProfile(file);
            apiCallTimer.stop(meterRegistry.timer("profilePicture.save"));
            logger.info("Profile picture uploaded successfully: {}", profile.getFileName());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "file_name", profile.getFileName(),
                    "id", profile.getId(),
                    "url", bucketName + "/" + profile.getFilePath(),
                    "upload_date", profile.getDateTime().toString()
            ));
        } catch (IOException e) {
            logger.error("Error saving profile picture: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFileMetadata(@PathVariable String id) {
        meterRegistry.counter("profilePicture.get.count").increment();
        Timer.Sample apiCallTimer = Timer.start(meterRegistry);
        logger.info("Fetching profile picture metadata for ID: {}", id);

        if (id == null || id.isEmpty()) {
            logger.warn("Invalid or empty ID provided for metadata fetch");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Profile profile = profileService.getProfilePicture(id);
        if (profile != null) {
            apiCallTimer.stop(meterRegistry.timer("profilePicture.get"));
            logger.info("Profile picture metadata retrieved successfully for ID: {}", id);
            return ResponseEntity.ok(Map.of(
                    "file_name", profile.getFileName(),
                    "id", profile.getId(),
                    "url", bucketName + "/" + profile.getFilePath(),
                    "upload_date", profile.getDateTime().toString()
            ));
        } else {
            logger.warn("Profile picture not found for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        meterRegistry.counter("profilePicture.delete.count").increment();
        Timer.Sample apiCallTimer = Timer.start(meterRegistry);
        logger.info("Deleting profile picture with ID: {}", id);

        if (id == null || id.isEmpty()) {
            logger.warn("Invalid or empty ID provided for delete operation");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (profileService.deleteProfile(id)) {
            apiCallTimer.stop(meterRegistry.timer("profilePicture.delete"));
            logger.info("Profile picture deleted successfully for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            logger.warn("Profile picture not found for deletion with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<Void> getFileWithoutId() {
        logger.warn("GET request made without ID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFileWithoutId() {
        logger.warn("DELETE request made without ID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowedFile() {
        logger.warn("Invalid method request for /v1/file");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowedFileId() {
        logger.warn("Invalid method request for /v1/file/{}", "{id}");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}
