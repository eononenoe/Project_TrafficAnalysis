package com.example.traffic.repository;

import com.example.traffic.entity.TrafficEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficRepository extends JpaRepository<TrafficEntity, Long> {
}
