package com.walletpet.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "user_name", nullable = false, unique = true, length = 50)
    private String userName;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "role", nullable = false, length = 50)
    private String role = "USER";
    /*
    @OneToMany(mappedBy="user",targetEntity=Category.class,
    		fetch = FetchType.LAZY)
    private Set<Category> categories;
    
    @OneToMany(mappedBy="user",targetEntity=Account.class,
    		fetch = FetchType.LAZY)
    private Set<Account> accounts;
    
    @OneToMany(mappedBy="user",targetEntity=Transaction.class,
    		fetch = FetchType.LAZY)
    private Set<Transaction> transactions;
    
    @OneToMany(mappedBy="user",targetEntity=Pet.class,
    		fetch = FetchType.LAZY)
    private Set<Pet> pets;
    
    @OneToMany(mappedBy="user",targetEntity=PetEvent.class,
    		fetch = FetchType.LAZY)
    private Set<PetEvent> events;
    
    @OneToMany(mappedBy="user",targetEntity=SavingGoal.class,
    		fetch = FetchType.LAZY)
    private Set<SavingGoal> goals;
    
    @OneToMany(mappedBy="user",targetEntity=Budget.class,
    		fetch = FetchType.LAZY)
    private Set<Budget> budgets;
    
    @OneToMany(mappedBy="user",targetEntity=DailyRecordReward.class,
    		fetch = FetchType.LAZY)
    private Set<DailyRecordReward> rewards;
    
    @OneToMany(mappedBy="user",targetEntity=AccountTransaction.class,
    		fetch = FetchType.LAZY)
    private Set<AccountTransaction> accountTrans;
    */
}
