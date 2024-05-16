package com.dekraspain.backend.template.modules.productOffering.domain.model;

import com.dekraspain.backend.template.shared.customValidators.EnumValidator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferingStatesDTO {

  @EnumValidator(enumClass = ProductOfferingStates.class)
  private ProductOfferingStates status;

  private Optional<List<String>> compliances;
}
