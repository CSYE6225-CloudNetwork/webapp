package com.CSYE6225.webapp.Config;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Config {

   public static S3Client getS3Client(String region){
      return S3Client.builder()
               .region(Region.of(region))
               .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
               .build();
   }

}
