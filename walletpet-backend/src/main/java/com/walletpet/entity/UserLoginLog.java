package com.walletpet.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "user_login_logs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_login_logs_user_date",
                columnNames = {"user_id", "login_date"}
        )
)
public class UserLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_log_id")
    private Long loginLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "login_date", nullable = false)
    private LocalDate loginDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
