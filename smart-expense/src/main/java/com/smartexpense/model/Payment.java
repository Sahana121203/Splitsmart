package com.smartexpense.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.smartexpense.model.enums.PaymentMethod;
import com.smartexpense.model.enums.PaymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String tripId;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private Double amount;

	@Enumerated(EnumType.STRING)
	private PaymentMethod method;

	@Enumerated(EnumType.STRING)
	private PaymentType type;

	private String reference;

	private String note;

	private String expenseId;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
