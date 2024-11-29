package com.dekraspain.backend.template.modules.auth.application.request;

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
    private LifeSpan lifeSpan;
    private Mandatee mandatee;
    private Mandator mandator;
    private List<Power> power;
    private Signer signer;
  }

  @Data
  public static class LifeSpan {

    private String startDateTime;
    private String endDateTime;
  }

  @Data
  public static class Mandatee {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String mobilePhone;
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
    private List<String> tmfAction;
    private String tmfDomain;
    private String tmfFunction;
    private String tmfType;
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
