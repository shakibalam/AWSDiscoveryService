package com.aws.dicoveryservices.service.impl;

import com.aws.dicoveryservices.entity.BucketFileDetails;
import com.aws.dicoveryservices.entity.EC2Instance;
import com.aws.dicoveryservices.entity.S3Bucket;
import com.aws.dicoveryservices.repository.BucketFileDetailsRepository;
import com.aws.dicoveryservices.repository.EC2InstanceRepository;
import com.aws.dicoveryservices.repository.S3BucketRepository;
import com.aws.dicoveryservices.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DiscoveryServiceImpl implements DiscoveryService {

    @Autowired
    private EC2InstanceRepository ec2InstanceRepository;

    @Autowired
    private S3BucketRepository s3BucketRepository;

    @Autowired
    private AwsCredentialsProvider awsCredentialsProvider;

    @Autowired
    private BucketFileDetailsRepository bucketFileDetailsRepository;

    private final Map<String, String> jobStatusMap = new ConcurrentHashMap<>();

    // method to get the ec2 instance list
    public List<String> getEc2InstanceList() {
        // get the ec2 client object
        Ec2Client ec2Client = Ec2Client.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.AP_SOUTH_1)
                .build();
        DescribeInstancesResponse ec2Instance = ec2Client.describeInstances();
        // get the list of ec2 instance
        return ec2Instance.reservations().get(0).instances().stream().map(Instance::instanceId).collect(Collectors.toList());
    }

    // method to get the s3 bucket list
    public List<String> getListS3Buckets() {
        ListBucketsResponse s3BucketsResponse = getS3ClientObject().listBuckets();
        return s3BucketsResponse.buckets().stream().map(Bucket::name).collect(Collectors.toList());
    }

    // method to get the s3 client object
    public S3Client getS3ClientObject() {
        return S3Client.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.AP_SOUTH_1)
                .build();
    }

    @Async
    public CompletableFuture<String> discoverEC2(String jobId) {
        // get the ec2 instance list
        List<String> ec2InstanceList = getEc2InstanceList();
        List<EC2Instance> ec2Instance = new ArrayList<>();
        ec2InstanceList.stream().forEach(data -> {
            EC2Instance ec2 = new EC2Instance(jobId, data);
            ec2Instance.add(ec2);
        });
        // save the instance list in db
        ec2InstanceRepository.saveAll(ec2Instance);
        return CompletableFuture.completedFuture("Job for EC2 Completed");
    }

    @Async
    public CompletableFuture<String> discoverS3(String jobId) {
        // get the s3 bucket list
        List<String> listS3Buckets = getListS3Buckets();
        List<S3Bucket> s3Buckets = new ArrayList<>();
        listS3Buckets.stream().forEach(data -> {
            S3Bucket s3 = new S3Bucket(jobId, data);
            s3Buckets.add(s3);
        });
        // save the bucket list in db
        s3BucketRepository.saveAll(s3Buckets);
        return CompletableFuture.completedFuture("Job for S3 Completed");
    }

    @Override
    public CompletableFuture<String> discoverServices(List<String> services) {
        // asynchronous tasks for EC2 and S3 discovery
        String jobId = UUID.randomUUID().toString();
        try {
            jobStatusMap.put(jobId, "In Progress");
            if (services.contains("EC2")) {
                // to fetch the ec2 services
                CompletableFuture<String> ec2JobResult = discoverEC2(jobId);
            }
            if (services.contains("S3")) {
                // to fetch the s3 bucket list
                CompletableFuture<String> s3JobResult = discoverS3(jobId);
            }
            jobStatusMap.put(jobId, "Success");
        } catch (Exception e) {
            jobStatusMap.put(jobId, "Failed");
        }
        return CompletableFuture.completedFuture(jobId);
    }

    @Override
    public String getJobResultByJobId(String jobId) {
        // to get the status by job id
        String status = "";
        for (Map.Entry<String, String> map : jobStatusMap.entrySet()) {
            if (map.getKey().equalsIgnoreCase(jobId)) {
                status = map.getValue();
                break;
            }
        }
        if (StringUtils.isEmpty(status)) {
            throw new RuntimeException("No such job id exist, please enter valid jobId");
        }
        return status;
    }

    @Override
    public List<String> getDiscoveryServiceDetails(String service) {
        // to get the list of ec2 services or s3 bucket
        List<String> list;
        if (service.equalsIgnoreCase("EC2")) {
            // to fetch ec2 services list
            list = getEc2InstanceList();
        } else if (service.equalsIgnoreCase("S3")) {
            // to fetch s3 bucket list
            list = getListS3Buckets();
        } else {
            throw new RuntimeException("Invalid service");
        }
        return list;
    }

    @Override
    public CompletableFuture<String> getS3BucketObjects(String bucketName) {
        // to fetch the s3 bucket objects
        CompletableFuture<String> jobResult = new CompletableFuture<>();
        try {
            // to get the s3 client object
            S3Client s3Client = getS3ClientObject();
            CompletableFuture.runAsync(() -> {
                try {
                    // to Complete the job with a unique JobId
                    String jobId = UUID.randomUUID().toString();

                    ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .build();
                    ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

                    // Iterating over the objects and save the details to db
                    for (S3Object s3Object : listObjectsResponse.contents()) {
                        BucketFileDetails bucketFileDetails = new BucketFileDetails(bucketName, s3Object.key(), jobId);
                        //save the details in db
                        bucketFileDetailsRepository.save(bucketFileDetails);
                    }
                    jobResult.complete(jobId);
                } catch (Exception e) {
                    jobResult.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            jobResult.completeExceptionally(e);
        }
        return jobResult;
    }

    @Override
    public Integer getS3BucketObjectCount(String bucketName) {
        return bucketFileDetailsRepository.findByBucketName(bucketName);
    }

    @Override
    public List<String> getS3BucketObjectlike(String bucketName, String pattern) {
        List<String> fileNames = new ArrayList<>();
        try {
            // to get the s3 bucket object by pattern
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
            ListObjectsV2Response response = getS3ClientObject().listObjectsV2(request);
            for (S3Object objectSummary : response.contents()) {
                // Add only that file names that match the pattern
                if (objectSummary.key().matches(pattern)) {
                    fileNames.add(objectSummary.key());
                }
            }
            // return the list of files which match the pattern
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while fetching details by bucket name :" + e.getMessage());
        }
        return fileNames;
    }
}
