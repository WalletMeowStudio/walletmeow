package com.walletpet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Entity
@Table(name = "savinggoal")
@EqualsAndHashCode(exclude = {"user", "account"}) 
@Data
public class SavingGoal {
	@Id
	private String savingGoalid;
	@Column(name = "goalName", length = 100)
	private String goalName;
	private BigDecimal targetAmount;
	private LocalDate startDate;
	private LocalDate endDate;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
    private User user;
	@OneToOne
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
	
	@Column(name = "final_amount")
	private BigDecimal finalAmount;      // 存下達成那一刻的金額

	@Column(name = "final_account_name")
	private String finalAccountName;     // 存下當時的帳戶名稱（寫死成字串）

	@Column(name = "status")
	private String status = "ACTIVE";

}
