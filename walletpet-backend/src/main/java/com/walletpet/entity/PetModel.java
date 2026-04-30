package com.walletpet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pet_model")
@Data
public class PetModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "petmodel_id")
    private Integer petModelId;

    @Column(name = "rive_name", nullable = false, length = 45)
    private String riveName;

    @Column(name = "description", nullable = false, length = 225)
    private String description;
}
