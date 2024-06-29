package com.example.fungid.repository;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassificationRepository extends JpaRepository<MushroomInstance, Long> {
    List<MushroomInstance> findAllByUser(User user);
}
