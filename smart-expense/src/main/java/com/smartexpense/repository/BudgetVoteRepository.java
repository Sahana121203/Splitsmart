package com.smartexpense.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartexpense.model.BudgetVote;

public interface BudgetVoteRepository extends JpaRepository<BudgetVote, String> {

	boolean existsByTripIdAndUserId(String tripId, String userId);

	int countByTripId(String tripId);

	Optional<BudgetVote> findByTripIdAndUserId(String tripId, String userId);

	@Query("SELECT MIN(v.maxBudget) FROM BudgetVote v WHERE v.trip.id = :tripId")
	Double findMinBudget(@Param("tripId") String tripId);

	@Query("SELECT MAX(v.maxBudget) FROM BudgetVote v WHERE v.trip.id = :tripId")
	Double findMaxBudget(@Param("tripId") String tripId);

	@Query("SELECT AVG(v.maxBudget) FROM BudgetVote v WHERE v.trip.id = :tripId")
	Double findAvgBudget(@Param("tripId") String tripId);

	@Query("SELECT v.maxBudget FROM BudgetVote v WHERE v.trip.id = :tripId ORDER BY v.maxBudget ASC")
	List<Double> findAllBudgetsSorted(@Param("tripId") String tripId);
}
