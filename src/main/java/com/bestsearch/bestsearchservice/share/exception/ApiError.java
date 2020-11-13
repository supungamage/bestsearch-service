package com.bestsearch.bestsearchservice.share.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiError {
	private int status;
	private String message;
	private boolean success = false;

	ApiError() {
	}

	ApiError(int status) {
		this.status = status;
	}

	ApiError(int status, Throwable ex) {
		this.status = status;
		this.message = ex.getLocalizedMessage();
	}

	ApiError(int status, String message) {
		this.status = status;
		this.message = message;
	}
}
