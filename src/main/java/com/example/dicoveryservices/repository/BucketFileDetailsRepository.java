package com.example.dicoveryservices.repository;

import com.example.dicoveryservices.entity.BucketFileDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BucketFileDetailsRepository extends JpaRepository<BucketFileDetails, Long> {

    @Query(value = "SELECT count(*) FROM bucket_file_details e WHERE e.bucket_name = :columnName", nativeQuery = true)
    Integer findByBucketName(@Param("columnName") String bucketName);
}
