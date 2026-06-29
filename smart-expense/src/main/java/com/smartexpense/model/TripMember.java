package com.smartexpense.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.smartexpense.model.enums.MemberRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trip_members", uniqueConstraints = @UniqueConstraint(columnNames = { "trip_id", "user_id" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripMember {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private MemberRole role = MemberRole.MEMBER;

	@Builder.Default
	private Double contribution = 0.0;

	@Builder.Default
	private Double consumption = 0.0;

	@Builder.Default
	private Double externalPaid = 0.0;

	private Double proratedContribution;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime joinedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trip_id", nullable = false)
	private Trip trip;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
