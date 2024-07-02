package com.example.fungid.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mushroom_instances")
public class MushroomInstance {

    public MushroomInstance() {
    }

    public MushroomInstance(User user, String classificationResult, String mushroomImageName, LocalDateTime sampleTakenAt) {
        this.user = user;
        this.classificationResult = classificationResult;
        this.mushroomImageName = mushroomImageName;
        this.sampleTakenAt = sampleTakenAt;
    }

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private User user;

    @Column(length = 100)
    private String classificationResult;

    @Column(nullable = false, unique = true, length = 100)
    private String mushroomImageName;

    @Column(nullable = false)
    private LocalDateTime sampleTakenAt;

    public Long getId() {
        return id;
    }

    public String getClassificationResult() {
        return classificationResult;
    }

    public String getMushroomImageName() {
        return mushroomImageName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getSampleTakenAt() {
        return sampleTakenAt;
    }

}
