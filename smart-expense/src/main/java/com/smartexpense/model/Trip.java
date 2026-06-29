package com.smartexpense.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.smartexpense.model.enums.TripStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String name;

	private String destination;

	private LocalDate startDate;

	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private TripStatus status = TripStatus.PLANNING;

	@Column(nullable = false)
	private String organizerId;

	@Builder.Default
	private Double kittyTarget = 0.0;

	@Builder.Default
	private Double kittyBalance = 0.0;

	@Builder.Default
	private String baseCurrency = "INR";

	@Builder.Default
	private Integer budgetVoteQuorumPercent = 50;

	private LocalDateTime budgetVoteDeadline;

	@Builder.Default
	private Boolean budgetVoteClosed = false;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<TripMember> members = new ArrayList<>();
}
