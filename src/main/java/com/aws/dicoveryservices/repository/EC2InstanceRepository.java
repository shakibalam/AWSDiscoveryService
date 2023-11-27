package com.aws.dicoveryservices.repository;

import com.aws.dicoveryservices.entity.EC2Instance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EC2InstanceRepository extends JpaRepository<EC2Instance, Long> {
}
