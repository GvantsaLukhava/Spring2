package com.inventory.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // WICHTIGER IMPORT!
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data        // Lombok: generates getters, setters, toString, equals, hashCode
@Builder     // Lombok: gives us a builder pattern
@NoArgsConstructor  // Lombok: generates no-args constructor (required by JPA)
@AllArgsConstructor // Lombok: generates all-args constructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment in DB
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Das Feld wurde nach unten konsolidiert und mit der Jackson-Annotation ausgestattet:
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference // Verhindert die Endlosschleife bei der JSON-Umwandlung
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // JPA lifecycle callbacks - automatically set timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}