package com.smartexpense.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.smartexpense.model.enums.ExpenseCategory;
import com.smartexpense.model.enums.PaymentSource;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private Double amount;

	@Builder.Default
	private String currency = "INR";

	private Double amountInBase;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private ExpenseCategory category = ExpenseCategory.OTHER;

	@Column(nullable = false)
	private String paidByUserId;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private PaymentSource paidFrom = PaymentSource.KITTY;

	private String receiptUrl;

	private String voiceRaw;

	@Builder.Default
	private Boolean editPending = false;

	private String editRequestedBy;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String pendingEditJson;

	@Builder.Default
	private Boolean deleted = false;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime editedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trip_id", nullable = false)
	private Trip trip;

	@OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ExpenseParticipant> participants = new ArrayList<>();
}
