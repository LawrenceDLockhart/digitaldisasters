package com.example.digitaldisasters.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
public class S3StorageService {
    private final AmazonS3 s3Client;
    private final String bucketName;

    public S3StorageService(
            @Value("${aws.access.key.id}") String accessKey,
            @Value("${aws.endpoint.url.s3}") String endpoint,
            @Value("${aws.region}") String region,
            @Value("${aws.secret.access.key}") String secretKey,
            @Value("${bucket.name}") String bucketName
    ) {
        this.bucketName = bucketName;

        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    public void uploadFile(String filename, InputStream content) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.available());  // Set the content length
        s3Client.putObject(bucketName, filename, content, metadata);
    }

    public InputStream getFile(String filename) {
        return s3Client.getObject(bucketName, filename).getObjectContent();
    }

    public String generatePresignedUrl(String key, HttpMethod method, int expirationInSeconds) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, key)
                        .withMethod(method)
                        .withExpiration(new Date(System.currentTimeMillis() + expirationInSeconds * 1000));
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(bucketName, key);
    }
}
