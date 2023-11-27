package com.aws.dicoveryservices.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bucket_file_details")
public class BucketFileDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String bucketName;
    private String file;
    private String jobId;

    public BucketFileDetails(String bucketName, String file, String jobId) {
        this.bucketName = bucketName;
        this.file = file;
        this.jobId = jobId;
    }
}
