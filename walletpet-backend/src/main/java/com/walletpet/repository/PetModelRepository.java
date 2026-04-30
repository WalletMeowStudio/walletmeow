package com.walletpet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.PetModel;

public interface PetModelRepository extends JpaRepository<PetModel, Integer> {
}