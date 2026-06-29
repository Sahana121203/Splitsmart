package com.smartexpense.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartexpense.model.Payment;
import com.smartexpense.model.enums.PaymentType;

public interface PaymentRepository extends JpaRepository<Payment, String> {

	List<Payment> findByTripIdAndTypeOrderByCreatedAtDesc(String tripId, PaymentType type);

	List<Payment> findByTripIdOrderByCreatedAtDesc(String tripId);

	List<Payment> findByTripIdAndUserId(String tripId, String userId);

	@Query("SELECT SUM(p.amount) FROM Payment p WHERE p.tripId = :tripId AND p.type = 'KITTY_DEPOSIT'")
	Double sumKittyDepositsByTripId(@Param("tripId") String tripId);

	@Query("SELECT SUM(p.amount) FROM Payment p WHERE p.tripId = :tripId AND p.userId = :userId AND p.type = 'KITTY_DEPOSIT'")
	Double sumKittyDepositsByTripIdAndUserId(@Param("tripId") String tripId, @Param("userId") String userId);
}
