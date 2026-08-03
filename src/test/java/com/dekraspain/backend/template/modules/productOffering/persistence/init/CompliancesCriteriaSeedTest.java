package com.dekraspain.backend.template.modules.productOffering.persistence.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesCriteriaEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.CompliancesCriteriaRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Runs the real {@link CompliancesCriteriaDataLoader} against a live Postgres and asserts the seed:
 * 31 criteria total — 24 Baseline (DP-1..5, CS-1..19) + 7 Professional (CS-20, PT-1,2, ST-1..4).
 * No full Spring context (no OAuth/mail/JWT beans) — just the JPA slice + the loader.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CompliancesCriteriaDataLoader.class)
@TestPropertySource(
  properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/dome",
    "spring.datasource.username=brunopereira",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
  }
)
class CompliancesCriteriaSeedTest {

  @Autowired private CompliancesCriteriaDataLoader loader;
  @Autowired private CompliancesCriteriaRepository repository;

  @Test
  void seedsAll31CriteriaWithCorrectLabelLevels() throws Exception {
    loader.run();

    List<CompliancesCriteriaEntity> all = repository.findAll();
    assertEquals(31, all.size(), "expected 31 seeded criteria");

    Map<String, Long> byLevel =
        all.stream()
            .collect(Collectors.groupingBy(CompliancesCriteriaEntity::getLabelLevel, Collectors.counting()));
    assertEquals(24L, byLevel.get("BL"), "24 Baseline criteria (DP-1..5 + CS-1..19)");
    assertEquals(7L, byLevel.get("P"), "7 Professional criteria (CS-20 + PT-1,2 + ST-1..4)");

    CompliancesCriteriaEntity cs20 =
        all.stream().filter(c -> "CS-20".equals(c.getCode())).findFirst().orElseThrow();
    assertEquals("P", cs20.getLabelLevel(), "CS-20 is Professional-tier");
    assertEquals("CYBERSECURITY", cs20.getCategory());

    assertTrue(
        all.stream().anyMatch(c -> "PT-1".equals(c.getCode()) && "P".equals(c.getLabelLevel())),
        "PT-1 present and Professional");
    assertTrue(
        all.stream().anyMatch(c -> "ST-4".equals(c.getCode()) && "SUSTAINABILITY".equals(c.getCategory())),
        "ST-4 present and Sustainability");
  }

  @Test
  void reRunIsIdempotent() throws Exception {
    loader.run();
    loader.run(); // guarded by existsByCodeAndRulesVersion — must not duplicate
    assertEquals(31, repository.findAll().size(), "re-running the loader must not duplicate rows");
  }
}
