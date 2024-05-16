package com.dekraspain.backend.template.shared.customResponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

  private int statusCode;
  private String message;
  private T data;

  // Constructor para respuestas de error
  public ApiResponse(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }
}
