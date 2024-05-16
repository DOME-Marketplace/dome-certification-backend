package com.dekraspain.backend.template.modules.private2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Private Route 2")
@RequestMapping("/api/v1")
@RestController
@CrossOrigin({ "http://localhost:4200" })
public class PrivateRoute2Controller {

  @GetMapping(value = "private-route-2")
  @SuppressWarnings("rawtypes")
  @Operation(security = { @SecurityRequirement(name = "bearer-key") })
  public ResponseEntity getAll() {
    return ResponseEntity.ok("Private Route");
  }
}
