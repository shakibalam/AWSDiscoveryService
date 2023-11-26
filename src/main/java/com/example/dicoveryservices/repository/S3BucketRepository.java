package com.example.dicoveryservices.repository;

import com.example.dicoveryservices.entity.S3Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface S3BucketRepository extends JpaRepository<S3Bucket, Long> {

}
