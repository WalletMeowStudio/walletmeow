package com.walletpet.entity;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walletpet.enums.CategoryType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "categories")
@EqualsAndHashCode(exclude = "user")
@EntityListeners(AuditingEntityListener.class)
@Data
public class Category {

    @Id
    @Column(name = "category_id", length = 50)
    private String categoryId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 20)
    private CategoryType categoryType;

    @Column(name = "icon", nullable = false, length = 50)
    private String icon = "default";

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "is_disable", nullable = false)
    private Boolean isDisable = false;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Transaction> transactions;
}
