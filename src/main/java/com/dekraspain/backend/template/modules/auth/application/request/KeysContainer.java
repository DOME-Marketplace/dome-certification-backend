package com.dekraspain.backend.template.modules.auth.application.request;

import java.util.List;
import lombok.Data;

@Data
public class KeysContainer {

  private List<Key> keys;

  @Data
  public static class Key {

    private String crv; // Curve
    private String kid; // Key ID
    private String kty; // Key Type
    private String x; // x-coordinate of the key
    private String y; // y-coordinate of the key
  }
}
