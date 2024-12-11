package com.dekraspain.backend.template.modules.auth.application.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import lombok.Data;

@Data
public class VerifiableCredentialPayload {

  private String aud;
  private String sub;
  private String scope;
  private String iss;
  private long exp;
  private long iat;
  private String jti;
  private VerifiableCredential vc;

  @Data
  public static class VerifiableCredential {

    private List<String> context;
    private String id;
    private List<String> type;
    private CredentialSubject credentialSubject;
    private String expirationDate;
    private String issuanceDate;
    private String issuer;
    private String validFrom;
    private String validUntil;
  }

  @Data
  public static class CredentialSubject {

    private Mandate mandate;
  }

  @Data
  public static class Mandate {

    private String id;
    private LifeSpan life_span;
    private Mandatee mandatee;
    private Mandator mandator;
    private List<Power> power;
    private Signer signer;
  }

  @Data
  public static class LifeSpan {

    private String start_date_time;
    private String end_date_time;
  }

  @Data
  public static class Mandatee {

    private String id;
    private String email;
    private String first_name;
    private String last_name;
    private String mobile_phone;
  }

  @Data
  public static class Mandator {

    private String commonName;
    private String country;
    private String emailAddress;
    private String organization;
    private String organizationIdentifier;
    private String serialNumber;
  }

  @Data
  public static class Power {

    private String id;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> tmf_action;

    private String tmf_domain;
    private String tmf_function;
    private String tmf_type;
  }

  @Data
  public static class Signer {

    private String commonName;
    private String country;
    private String emailAddress;
    private String organization;
    private String organizationIdentifier;
    private String serialNumber;
  }
}
