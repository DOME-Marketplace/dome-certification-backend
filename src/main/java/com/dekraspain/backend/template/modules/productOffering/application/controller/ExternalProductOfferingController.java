package com.dekraspain.backend.template.modules.productOffering.application.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/external-product-offering")
@RequiredArgsConstructor
public class ExternalProductOfferingController {

    private final RestTemplate restTemplate;
    private static final String[] ALLOWED_DOMAINS = {
        "https://dome-marketplace-sbx.org/",
        "https://dome-marketplace-dev2.org/",
        "https://dome-marketplace.eu/"
    };

    @GetMapping("/proxy")
    public ResponseEntity<String> proxyExternalProductOffering(String url) {
        if (url == null) {
            return ResponseEntity.badRequest().body("Missing url param");
        }
        String matchedDomain = null;
        for (String domain : ALLOWED_DOMAINS) {
            if (url.startsWith(domain)) {
                matchedDomain = domain;
                break;
            }
        }
        if (matchedDomain == null) {
            return ResponseEntity.status(400).body("URL not allowed");
        }
        // Extraer el id del final de la url recibida
        int lastColon = url.lastIndexOf(":");
        if (lastColon == -1) {
            return ResponseEntity.badRequest().body("Invalid product offering id in url");
        }
        // Reconstruir la url destino
        String fullId = url.substring(url.lastIndexOf("urn:ngsi-ld:product-offering:"));
        String targetUrl = matchedDomain + "catalog/productOffering/" + fullId;
        ResponseEntity<String> response = restTemplate.getForEntity(targetUrl, String.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(response.getStatusCode()).headers(headers).body(response.getBody());
    }
}
