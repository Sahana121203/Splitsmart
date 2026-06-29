package com.smartexpense.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartexpense.model.Expense;
import com.smartexpense.model.enums.ExpenseCategory;

public interface ExpenseRepository extends JpaRepository<Expense, String> {

	List<Expense> findByTripIdAndDeletedFalse(String tripId);

	List<Expense> findByTripIdAndDeletedFalseAndCategory(String tripId, ExpenseCategory category);

	List<Expense> findByTripIdAndDeletedFalseAndPaidByUserId(String tripId, String paidByUserId);

	@Query("SELECT e FROM Expense e WHERE e.trip.id = :tripId AND e.deleted = false AND e.createdAt BETWEEN :start AND :end")
	List<Expense> findByTripIdAndDateRange(
			@Param("tripId") String tripId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	@Query("SELECT SUM(ep.share) FROM ExpenseParticipant ep WHERE ep.expense.trip.id = :tripId AND ep.userId = :userId AND ep.expense.deleted = false")
	Double sumConsumptionByTripIdAndUserId(@Param("tripId") String tripId, @Param("userId") String userId);
}
