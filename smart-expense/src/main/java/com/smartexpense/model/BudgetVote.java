package com.smartexpense.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budget_votes", uniqueConstraints = @UniqueConstraint(columnNames = { "trip_id", "user_id" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetVote {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private Double maxBudget;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime submittedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trip_id", nullable = false)
	private Trip trip;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
