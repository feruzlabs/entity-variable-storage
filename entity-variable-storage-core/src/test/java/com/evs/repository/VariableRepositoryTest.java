package com.evs.repository;

import com.evs.model.Entity;
import com.evs.model.EntityInstance;
import com.evs.model.Variable;
import com.evs.model.VariableType;
import com.evs.service.PartitionManager;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class VariableRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("evs_test")
            .withUsername("test")
            .withPassword("test");

    private VariableRepository variableRepository;
    private EntityInstanceRepository instanceRepository;
    private UUID entityId;
    private UUID instanceId;

    @BeforeEach
    void setUp() {
        DataSource ds = createDataSource();
        runMigrations(ds);
        EntityRepository entityRepository = new EntityRepository(ds);
        instanceRepository = new EntityInstanceRepository(ds);
        variableRepository = new VariableRepository(ds);

        Entity entity = entityRepository.save(Entity.builder().name("User").displayName("User").build());
        entityId = entity.id();
        new PartitionManager(ds).createEntityPartition(entityId, "User");

        EntityInstance instance = instanceRepository.save(
                EntityInstance.builder().entityId(entityId).uuid(UUID.randomUUID()).build()
        );
        instanceId = instance.id();
    }

    private DataSource createDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(postgres.getJdbcUrl());
        ds.setUsername(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        return ds;
    }

    private void runMigrations(DataSource ds) {
        org.flywaydb.core.Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    void shouldSaveAndFindVariable() {
        EntityInstance instance = instanceRepository.findById(instanceId).orElseThrow();
        Variable variable = Variable.builder()
                .entityId(entityId)
                .entityInstanceId(instanceId)
                .variableName("email")
                .variableType(VariableType.STRING)
                .value("test@example.com")
                .registeredAt(instance.registeredAt())
                .build();

        Variable saved = variableRepository.save(variable);

        assertNotNull(saved.id());
        assertEquals("test@example.com", saved.asString());

        Optional<Variable> found = variableRepository.findByInstanceIdAndName(instanceId, "email");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().asString());
    }

    @Test
    void shouldSaveBatch() {
        EntityInstance instance = instanceRepository.findById(instanceId).orElseThrow();
        List<Variable> variables = List.of(
                Variable.builder().entityId(entityId).entityInstanceId(instanceId)
                        .variableName("a").variableType(VariableType.STRING).value("1")
                        .registeredAt(instance.registeredAt()).build(),
                Variable.builder().entityId(entityId).entityInstanceId(instanceId)
                        .variableName("b").variableType(VariableType.STRING).value("2")
                        .registeredAt(instance.registeredAt()).build()
        );

        List<Variable> saved = variableRepository.saveBatch(variables);

        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(v -> v.id() != null));

        List<Variable> found = variableRepository.findByEntityInstanceId(instanceId);
        assertEquals(2, found.size());
    }

    @Test
    void shouldHandleJsonVariable() {
        EntityInstance instance = instanceRepository.findById(instanceId).orElseThrow();
        Variable variable = Variable.builder()
                .entityId(entityId)
                .entityInstanceId(instanceId)
                .variableName("data")
                .variableType(VariableType.JSON)
                .value(Map.of("key", "value"))
                .registeredAt(instance.registeredAt())
                .build();

        Variable saved = variableRepository.save(variable);
        assertNotNull(saved.id());

        Map<String, Object> data = saved.asJson();
        assertEquals("value", data.get("key"));
    }
}
