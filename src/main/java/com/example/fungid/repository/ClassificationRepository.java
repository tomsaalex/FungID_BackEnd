package com.example.fungid.repository;

import com.example.fungid.domain.MushroomInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassificationRepository extends JpaRepository<MushroomInstance, Long> {
}
