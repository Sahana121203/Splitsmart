package com.smartexpense.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartexpense.model.User;

public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findByPhone(String phone);

	Optional<User> findByEmail(String email);

	boolean existsByPhone(String phone);

	boolean existsByEmail(String email);
}
