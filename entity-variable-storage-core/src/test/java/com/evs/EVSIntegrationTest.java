package com.evs;

import com.evs.config.EVSConfig;
import com.evs.config.EVSFactory;
import com.evs.model.Entity;
import com.evs.model.EntityInstance;
import com.evs.model.Variable;
import com.evs.model.VariableType;
import com.evs.service.EntityInstanceService;
import com.evs.service.EntityService;
import com.evs.service.VariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class EVSIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("evs_test")
            .withUsername("test")
            .withPassword("test");

    private EVSFactory factory;
    private EntityService entityService;
    private EntityInstanceService instanceService;
    private VariableService variableService;

    @BeforeEach
    void setUp() {
        EVSConfig config = new EVSConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(5);

        factory = new EVSFactory(config);
        entityService = factory.entityService();
        instanceService = factory.entityInstanceService();
        variableService = factory.variableService();
    }

    @Test
    void shouldCreateEntityAndPartitions() {
        Entity entity = Entity.builder()
                .name("TestEntity")
                .displayName("Test Entity")
                .description("Integration test entity")
                .build();

        Entity saved = entityService.createEntity(entity);

        assertNotNull(saved.id());
        assertEquals("TestEntity", saved.name());
        assertEquals("Test Entity", saved.displayName());
    }

    @Test
    void shouldCreateEntityInstance() {
        Entity entity = entityService.createEntity(
                Entity.builder().name("Order").displayName("Order").build()
        );

        EntityInstance instance = EntityInstance.builder()
                .entityId(entity.id())
                .uuid(UUID.randomUUID())
                .build();

        EntityInstance saved = instanceService.createInstance(instance);

        assertNotNull(saved.id());
        assertEquals(entity.id(), saved.entityId());
        assertNotNull(saved.registeredAt());
    }

    @Test
    void shouldCreateAndRetrieveVariables() {
        Entity entity = entityService.createEntity(
                Entity.builder().name("User").displayName("User").build()
        );
        EntityInstance instance = instanceService.createInstance(
                EntityInstance.builder().entityId(entity.id()).uuid(UUID.randomUUID()).build()
        );

        List<Variable> variables = List.of(
                Variable.builder()
                        .entityId(entity.id())
                        .entityInstanceId(instance.id())
                        .variableName("name")
                        .variableType(VariableType.STRING)
                        .value("Alice")
                        .registeredAt(instance.registeredAt())
                        .build(),
                Variable.builder()
                        .entityId(entity.id())
                        .entityInstanceId(instance.id())
                        .variableName("score")
                        .variableType(VariableType.FLOAT)
                        .value(95.5)
                        .registeredAt(instance.registeredAt())
                        .build()
        );

        List<Variable> saved = variableService.createVariablesBatch(variables);

        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(v -> v.id() != null));

        Map<String, Object> varsMap = variableService.getVariablesAsMap(instance.id());
        assertEquals("Alice", varsMap.get("name"));
        assertEquals(95.5, varsMap.get("score"));
    }

    @Test
    void shouldFindEntityByName() {
        Entity entity = entityService.createEntity(
                Entity.builder().name("Invoice").displayName("Invoice").build()
        );

        Entity found = entityService.getEntityByName("Invoice");
        assertEquals(entity.id(), found.id());
    }

    @Test
    void shouldHandleJsonVariableType() {
        Entity entity = entityService.createEntity(
                Entity.builder().name("Config").displayName("Config").build()
        );
        EntityInstance instance = instanceService.createInstance(
                EntityInstance.builder().entityId(entity.id()).uuid(UUID.randomUUID()).build()
        );

        Variable variable = Variable.builder()
                .entityId(entity.id())
                .entityInstanceId(instance.id())
                .variableName("settings")
                .variableType(VariableType.JSON)
                .value(Map.of("theme", "dark", "notifications", true))
                .registeredAt(instance.registeredAt())
                .build();

        Variable saved = variableService.createVariable(variable);
        assertNotNull(saved.id());

        Map<String, Object> varsMap = variableService.getVariablesAsMap(instance.id());
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = (Map<String, Object>) varsMap.get("settings");
        assertEquals("dark", settings.get("theme"));
        assertEquals(true, settings.get("notifications"));
    }
}
