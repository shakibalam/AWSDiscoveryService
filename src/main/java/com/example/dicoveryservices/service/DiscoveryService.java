package com.example.dicoveryservices.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface DiscoveryService {
    CompletableFuture<String> discoverServices(List<String> services);

    String getJobResultByJobId(String jobId);

    List<String> getDiscoveryServiceDetails(String service);

    CompletableFuture<String> getS3BucketObjects(String bucketName) throws ExecutionException, InterruptedException;

    Integer getS3BucketObjectCount(String bucketName);

    List<String> getS3BucketObjectlike(String bucketName, String pattern);

}
