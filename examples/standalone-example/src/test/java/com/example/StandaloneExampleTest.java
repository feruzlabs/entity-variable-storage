package com.example;

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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class StandaloneExampleTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("evs_test")
            .withUsername("test")
            .withPassword("test");

    private EntityService entityService;
    private EntityInstanceService instanceService;
    private VariableService variableService;

    @BeforeEach
    void setUp() {
        EVSConfig config = new EVSConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());

        EVSFactory factory = new EVSFactory(config);
        entityService = factory.entityService();
        instanceService = factory.entityInstanceService();
        variableService = factory.variableService();
    }

    @Test
    void shouldRunFullFlowLikeStandaloneExample() {
        Entity userEntity = entityService.createEntity(
                Entity.builder()
                        .name("User")
                        .displayName("User Entity")
                        .description("User domain model")
                        .build()
        );
        assertNotNull(userEntity.id());

        EntityInstance instance = instanceService.createInstance(
                EntityInstance.builder()
                        .entityId(userEntity.id())
                        .uuid(UUID.randomUUID())
                        .build()
        );
        assertNotNull(instance.id());

        List<Variable> variables = List.of(
                Variable.builder()
                        .entityId(userEntity.id())
                        .entityInstanceId(instance.id())
                        .variableName("name")
                        .variableType(VariableType.STRING)
                        .value("John Doe")
                        .registeredAt(instance.registeredAt())
                        .build(),
                Variable.builder()
                        .entityId(userEntity.id())
                        .entityInstanceId(instance.id())
                        .variableName("age")
                        .variableType(VariableType.INTEGER)
                        .value(30L)
                        .registeredAt(instance.registeredAt())
                        .build(),
                Variable.builder()
                        .entityId(userEntity.id())
                        .entityInstanceId(instance.id())
                        .variableName("metadata")
                        .variableType(VariableType.JSON)
                        .value(Map.of("department", "Engineering"))
                        .registeredAt(instance.registeredAt())
                        .build()
        );

        variableService.createVariablesBatch(variables);

        Map<String, Object> vars = variableService.getVariablesAsMap(instance.id());
        assertEquals("John Doe", vars.get("name"));
        assertEquals(30L, vars.get("age"));
        assertEquals(Map.of("department", "Engineering"), vars.get("metadata"));
    }
}
