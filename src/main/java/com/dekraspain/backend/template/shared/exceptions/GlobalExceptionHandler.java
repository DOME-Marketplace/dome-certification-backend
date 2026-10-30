package com.dekraspain.backend.template.shared.exceptions;

import com.dekraspain.backend.template.shared.customResponses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingPart(
    MissingServletRequestPartException ex,
    HttpServletRequest request
  ) {
    log.warn(
      "Rejected {} {}: missing multipart part '{}'",
      request.getMethod(),
      request.getRequestURI(),
      ex.getRequestPartName()
    );
    ApiResponse<Void> response = new ApiResponse<>(
      HttpStatus.BAD_REQUEST.value(),
      "Missing required part: " + ex.getRequestPartName(),
      null
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
    log.error("Error interno del servidor", ex);
    ApiResponse<Void> response = new ApiResponse<>(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "Error interno del servidor",
      null
    );
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(response);
  }
}
