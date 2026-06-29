package com.smartexpense.util;

import java.time.LocalDateTime;

public class ApiResponse<T> {

	private boolean success;
	private T data;
	private String error;
	private LocalDateTime timestamp;

	public ApiResponse() {
	}

	public ApiResponse(boolean success, T data, String error, LocalDateTime timestamp) {
		this.success = success;
		this.data = data;
		this.error = error;
		this.timestamp = timestamp;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null, LocalDateTime.now());
	}

	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(false, null, message, LocalDateTime.now());
	}
}
