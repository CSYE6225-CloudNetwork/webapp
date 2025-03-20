package com.CSYE6225.webapp.Services;

import com.CSYE6225.webapp.Entity.Profile;
import com.CSYE6225.webapp.Repository.ProfileRepo;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.CSYE6225.webapp.Config.S3Config;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepo profileRepo;
    private final S3Client s3Client;
    private final String bucketName;
    private final String bucketRegion;

    public ProfileService(ProfileRepo profileRepo,@Value("${S3.BucketName}") String bucketName,
                          @Value("${S3.RegionName}") String bucketRegion) {
        this.profileRepo = profileRepo;
        this.bucketName = bucketName;
        this.bucketRegion = bucketRegion;


        this.s3Client = S3Config.getS3Client(bucketRegion);
    }

    public Profile saveProfile(MultipartFile file) throws IOException {

        String picName = UUID.randomUUID().toString();

        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);


        String path = "ProfilePicture/" + picName+"."+ext;


        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(path)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        Profile profile = new Profile();
        profile.setFileName(picName+"."+ext);
        profile.setFilePath(path);

        return profileRepo.save(profile);
    }

    public Profile getProfilePicture(String id) {
        Profile profile = profileRepo.findById(id).get();
        if (profile.equals(null) || profileRepo.findById(id).isEmpty()) {
            return profile;
        }
        return profileRepo.findById(id).get();
    }

    public boolean deleteProfile(String id) {
    //    Profile profile = profileRepo.findById(id).orElse(null);

//        if (profile != null || profile.getFilePath() != null) {
//            String path = profile.getFilePath();
//            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(path));
//            profileRepo.deleteById(id);
//
//            return true;
//        }

        if (!profileRepo.findById(id).equals(null) && !profileRepo.findById(id).isEmpty()) {
            Profile profile = profileRepo.findById(id).get();
            String path = profile.getFilePath();
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(path));
            profileRepo.deleteById(id);

            return true;
        }

        return false;
    }
}
