package com.example.fungid.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

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

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MushroomInstance that = (MushroomInstance) o;
        return Objects.equals(user, that.user) && Objects.equals(classificationResult, that.classificationResult) && Objects.equals(mushroomImageName, that.mushroomImageName) && Objects.equals(sampleTakenAt, that.sampleTakenAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(user);
        result = 31 * result + Objects.hashCode(classificationResult);
        result = 31 * result + Objects.hashCode(mushroomImageName);
        result = 31 * result + Objects.hashCode(sampleTakenAt);
        return result;
    }
}
