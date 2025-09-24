package com.dekraspain.backend.template.modules.productOffering.application.controller;

import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingComplianceProfileCriteriaDetailDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.service.ProductOfferingComplianceProfileCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.domain.service.ProductOfferingService;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compliances")
@RequiredArgsConstructor
public class ProductOfferingComplianceProfileCriteriaController {

    private final ProductOfferingComplianceProfileCriteriaService service;
    private final ProductOfferingService productOfferingService;

    @GetMapping("/by-product/{productOfferingId}")
    public ResponseEntity<List<ProductOfferingComplianceProfileCriteriaDetailDTO>> getByProductOffering(@PathVariable Long productOfferingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        boolean isAdminOrEmployee = UserRole.ADMIN.equals(user.getRole()) || UserRole.EMPLOYEE.equals(user.getRole());
        ProductOfferingEntity po = productOfferingService.getProductOfferingById(productOfferingId);
        boolean isOwner = po != null && po.getUser() != null && po.getUser().getId().equals(user.getId());
        if (!isAdminOrEmployee && !isOwner) {
            return ResponseEntity.status(403).build();
        }
        List<ProductOfferingComplianceProfileCriteriaDetailDTO> result = service.findDetailsByProductOfferingId(productOfferingId);
        return ResponseEntity.ok(result);
    }
}

