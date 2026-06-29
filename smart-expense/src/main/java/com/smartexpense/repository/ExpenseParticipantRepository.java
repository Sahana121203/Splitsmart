package com.smartexpense.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartexpense.model.ExpenseParticipant;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, String> {

	List<ExpenseParticipant> findByExpenseId(String expenseId);

	List<ExpenseParticipant> findByUserId(String userId);

	@Query("SELECT ep FROM ExpenseParticipant ep WHERE ep.expense.trip.id = :tripId AND ep.userId = :userId AND ep.expense.deleted = false")
	List<ExpenseParticipant> findByTripIdAndUserId(@Param("tripId") String tripId, @Param("userId") String userId);
}
