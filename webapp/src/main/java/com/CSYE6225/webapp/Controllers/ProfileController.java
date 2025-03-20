package com.CSYE6225.webapp.Controllers;

import com.CSYE6225.webapp.Entity.Profile;
import com.CSYE6225.webapp.Services.ProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/v1/file")
public class ProfileController {
    private final ProfileService profileService;

    @Value("${S3.BucketName}") String bucketName;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("profilePic") MultipartFile file) {
        if (file == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Profile profile = profileService.saveProfile(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "file_name", profile.getFileName(),
                    "id", profile.getId(),
                    "url", bucketName+"/" + profile.getFilePath(),
                    "upload_date", profile.getDateTime().toString()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFileMetadata(@PathVariable String id) {
        if (id == null || id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Profile profile = profileService.getProfilePicture(id);
        if (profile != null) {
            return ResponseEntity.ok(Map.of(
                    "file_name", profile.getFileName(),
                    "id", profile.getId(),
                    "url", bucketName+"/" + profile.getFilePath(),
                    "upload_date", profile.getDateTime().toString()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        if (id == null || id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (profileService.deleteProfile(id)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
        } else {
            System.out.println("Inside 69");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }
    }

    // Return 400 Bad Request if no ID is provided for GET /v1/file and DELETE /v1/file
    @GetMapping
    public ResponseEntity<Void> getFileWithoutId() {
        System.out.println("Inside this");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFileWithoutId() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
    }

    // Handle unsupported methods for /v1/file
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowedFile() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build(); // 405 Method Not Allowed
    }

    // Handle unsupported methods for /v1/file/{id}
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH})
    public ResponseEntity<Void> methodNotAllowedFileId() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build(); // 405 Method Not Allowed
    }
}
