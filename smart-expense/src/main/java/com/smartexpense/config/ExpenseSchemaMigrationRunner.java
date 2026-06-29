package com.smartexpense.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExpenseSchemaMigrationRunner implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		try {
			jdbcTemplate.execute("ALTER TABLE expenses MODIFY pending_edit_json LONGTEXT");
		} catch (Exception ignored) {
			// Table or column may not exist yet on first bootstrap; Hibernate will create it.
		}
	}
}
