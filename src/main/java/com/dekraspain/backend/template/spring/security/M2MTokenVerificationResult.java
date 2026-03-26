package com.dekraspain.backend.template.spring.security;

public class M2MTokenVerificationResult {
  public enum Reason {
    OK,
    EXPIRED,
    INVALID_SIGNATURE,
    INVALID_FORMAT,
    UNSUPPORTED_ALGORITHM,
    KID_NOT_FOUND,
    JWKS_FETCH_ERROR,
    OTHER
  }

  private final boolean valid;
  private final Reason reason;
  private final String message;

  private M2MTokenVerificationResult(boolean valid, Reason reason, String message) {
    this.valid = valid;
    this.reason = reason;
    this.message = message;
  }

  public static M2MTokenVerificationResult ok() {
    return new M2MTokenVerificationResult(true, Reason.OK, null);
  }

  public static M2MTokenVerificationResult expired(String msg) {
    return new M2MTokenVerificationResult(false, Reason.EXPIRED, msg);
  }

  public static M2MTokenVerificationResult invalidSignature(String msg) {
    return new M2MTokenVerificationResult(false, Reason.INVALID_SIGNATURE, msg);
  }

  public static M2MTokenVerificationResult invalidFormat(String msg) {
    return new M2MTokenVerificationResult(false, Reason.INVALID_FORMAT, msg);
  }

  public static M2MTokenVerificationResult unsupportedAlg(String msg) {
    return new M2MTokenVerificationResult(false, Reason.UNSUPPORTED_ALGORITHM, msg);
  }

  public static M2MTokenVerificationResult kidNotFound(String msg) {
    return new M2MTokenVerificationResult(false, Reason.KID_NOT_FOUND, msg);
  }

  public static M2MTokenVerificationResult jwksFetchError(String msg) {
    return new M2MTokenVerificationResult(false, Reason.JWKS_FETCH_ERROR, msg);
  }

  public static M2MTokenVerificationResult other(String msg) {
    return new M2MTokenVerificationResult(false, Reason.OTHER, msg);
  }

  public boolean isValid() {
    return valid;
  }

  public Reason getReason() {
    return reason;
  }

  public String getMessage() {
    return message;
  }
}
