package com.example.traffic.repository;

import com.example.traffic.entity.CctvInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CctvRepository extends JpaRepository<CctvInfo, Long> {
}
