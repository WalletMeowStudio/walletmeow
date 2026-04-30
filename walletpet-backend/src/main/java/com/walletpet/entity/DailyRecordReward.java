package com.walletpet.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walletpet.enums.RewardType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(
    name = "daily_record_rewards",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_reward_user_date", columnNames = {"user_id", "reward_date"})
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
public class DailyRecordReward {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "daily_reward_id")
	private Long dailyRewardId;

	@Column(name = "reward_date", nullable = false)
	private LocalDate rewardDate;

	@Column(name = "qualified", nullable = false)
	private Boolean qualified = false;

	@Column(name = "transaction_count", nullable = false)
	private Integer transactionCount = 0;

	@Column(name = "streak_days", nullable = false)
	private Integer streakDays = 0;

	@Enumerated(EnumType.STRING)
	@Column(name = "reward_type", length = 50)
	private RewardType rewardType;

	@Column(name = "reward_value")
	private Integer rewardValue;

	@Column(name = "mood_delta", nullable = false)
	private Integer moodDelta = 0;

	@Column(name = "claimed_at")
	private LocalDateTime claimedAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	@CreatedDate
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	@LastModifiedDate
	private LocalDateTime updatedAt;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
