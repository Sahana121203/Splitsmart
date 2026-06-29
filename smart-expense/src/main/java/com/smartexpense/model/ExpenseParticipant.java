package com.smartexpense.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private Double share;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expense_id", nullable = false)
	private Expense expense;
}
