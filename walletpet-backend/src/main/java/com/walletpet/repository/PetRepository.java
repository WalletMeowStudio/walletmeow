package com.walletpet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, String> {

    List<Pet> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    Optional<Pet> findByPetIdAndUser_UserId(String petId, String userId);
}