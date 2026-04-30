package com.walletpet.entity;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pets")
@EqualsAndHashCode(exclude = {"user", "model"})
@EntityListeners(AuditingEntityListener.class)
@Data
public class Pet {

    @Id
    @Column(name = "pet_id", length = 50)
    private String petId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private PetModel model;

    @Column(name = "pet_name", nullable = false, length = 45)
    private String petName;

    @Column(name = "mood", nullable = false)
    private Integer mood;

    @Column(name = "cancan", nullable = false)
    private Integer cancan;

    @Column(name = "last_update_at", nullable = false, insertable = false)
    @LastModifiedDate
    private LocalDateTime lastUpdateAt;

    @Column(name = "is_displayed", nullable = false)
    private Boolean isDisplayed = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "pet", fetch = FetchType.LAZY)
    private Set<PetEvent> events;
}
