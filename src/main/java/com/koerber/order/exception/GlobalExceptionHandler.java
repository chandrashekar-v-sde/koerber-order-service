package com.koerber.order.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.koerber.order.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFound(final OrderNotFoundException ex) {

		logger.error("Order not found: {}", ex.getMessage());

		return new ResponseEntity<>(new ErrorResponse(404, ex.getMessage()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(InventoryServiceException.class)
	public ResponseEntity<ErrorResponse> handleInventoryError(final InventoryServiceException ex) {

		logger.error("Inventory service error: {}", ex.getMessage());

		return new ResponseEntity<>(new ErrorResponse(400, ex.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(final Exception ex) {

		logger.error("Unexpected error", ex);

		return new ResponseEntity<>(new ErrorResponse(500, "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
