package com.smartexpense.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartexpense.model.Trip;

public interface TripRepository extends JpaRepository<Trip, String> {

	List<Trip> findByOrganizerId(String organizerId);

	List<Trip> findDistinctByMembersUserIdOrderByCreatedAtDesc(String userId);
}
