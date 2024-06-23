package com.example.fungid.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "mushroom_instances")
public class MushroomInstance {

    public MushroomInstance() {
    }

    public MushroomInstance(Long id, String classificationResult, String mushroomImageName) {
        this.id = id;
        this.classificationResult = classificationResult;
        this.mushroomImageName = mushroomImageName;
    }

    public MushroomInstance(User user, String classificationResult, String mushroomImageName) {
        this.user = user;
        this.classificationResult = classificationResult;
        this.mushroomImageName = mushroomImageName;
    }

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private User user;

    @Column(nullable = true, unique = false, length = 50)
    private String classificationResult;

    @Column(nullable = false, unique = true, length = 100)
    private String mushroomImageName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassificationResult() {
        return classificationResult;
    }

    public void setClassificationResult(String classificationResult) {
        this.classificationResult = classificationResult;
    }

    public String getMushroomImageName() {
        return mushroomImageName;
    }

    public void setMushroomImageName(String mushroomImagePath) {
        this.mushroomImageName = mushroomImagePath;
    }
}
