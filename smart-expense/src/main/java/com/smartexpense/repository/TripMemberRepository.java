package com.smartexpense.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartexpense.model.TripMember;

public interface TripMemberRepository extends JpaRepository<TripMember, String> {

	Optional<TripMember> findByTripIdAndUserId(String tripId, String userId);

	List<TripMember> findByTripId(String tripId);

	boolean existsByTripIdAndUserId(String tripId, String userId);

	int countByTripId(String tripId);
}
