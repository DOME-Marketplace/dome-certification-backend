package com.dekraspain.backend.template.modules.productOffering.domain.model;

import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.shared.customValidators.EnumValidator;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferingDTO {

  public Long id;
  public String id_PO;
  public String service_name;
  public String service_version;
  public String name_organization;
  public String address_organization;
  public String ISO_Country_Code;
  public String url_organization;
  public String email_organization;
  public String VAT_ID;
  public String comments;
  public UserDTO issuer;
  public UserDTO user;
  public String image;
  public List<ComplianceDTO> compliances;

  public RequestedComplianceLevel requestedComplianceLevel;

  @EnumValidator(enumClass = ProductOfferingStates.class)
  public ProductOfferingStates status;

  public Date request_date;
  public Date issue_date;
  public Date expiration_date;

  public List<CompilanceProfileDTO> complianceProfiles;
}
