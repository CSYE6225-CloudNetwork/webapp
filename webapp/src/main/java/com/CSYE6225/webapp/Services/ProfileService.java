package com.CSYE6225.webapp.Services;

import com.CSYE6225.webapp.Controllers.ProfileController;
import com.CSYE6225.webapp.Entity.Profile;
import com.CSYE6225.webapp.Repository.ProfileRepo;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import io.micrometer.core.instrument.Timer;


@Service
public class ProfileService {

    private final ProfileRepo profileRepo;
    private final S3Client s3Client;
    private final String bucketName;
    private final String bucketRegion;
    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private MeterRegistry meterRegistry;

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

        Timer.Sample s3uploadTime = Timer.start(meterRegistry);
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(path)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        logger.info("saved profile picture to S3:{}",file.getOriginalFilename());
        s3uploadTime.stop(meterRegistry.timer("profilePicture.time"));
        Profile profile = new Profile();
        profile.setFileName(picName+"."+ext);
        profile.setFilePath(path);

        Timer.Sample dbUploadTime = Timer.start(meterRegistry);
        Profile savedProfile = profileRepo.save(profile);
        dbUploadTime.stop(meterRegistry.timer("profilePicturedb.time"));
        logger.info("profile picture record saved to db:{}",savedProfile.getId());
        return savedProfile;
    }

    public Profile getProfilePicture(String id) {
        Timer.Sample dbGetTime = Timer.start(meterRegistry);
        Profile profile = profileRepo.findById(id).get();
        if (profile.equals(null) || profileRepo.findById(id).isEmpty()) {
            dbGetTime.stop(meterRegistry.timer("profilePicturedb.time"));
            return profile;
        }
        dbGetTime.stop(meterRegistry.timer("profilePicturedb.time"));
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
        Timer.Sample dbDeleteTime = Timer.start(meterRegistry);
        if (!profileRepo.findById(id).equals(null) && !profileRepo.findById(id).isEmpty()) {
            Profile profile = profileRepo.findById(id).get();
            String path = profile.getFilePath();
            Timer.Sample s3deleteTime = Timer.start(meterRegistry);
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(path));
            s3deleteTime.stop(meterRegistry.timer("profilePicture.delete.time"));
            profileRepo.deleteById(id);
            dbDeleteTime.stop(meterRegistry.timer("delete.profilePictureDB.time"));
            logger.info("profile picture record deleted from db:{}",id);
            return true;
        }
        logger.info("invalid id for delete profile picture record:{}",id);
        dbDeleteTime.stop(meterRegistry.timer("delete.profilePictureDB.time"));
        return false;
    }
}
