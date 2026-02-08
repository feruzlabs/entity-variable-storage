package com.evs.repository;

import com.evs.model.Entity;
import com.evs.model.EntityInstance;
import com.evs.model.EntityInstanceStatus;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class EntityInstanceRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("evs_test")
            .withUsername("test")
            .withPassword("test");

    private EntityInstanceRepository instanceRepository;
    private EntityRepository entityRepository;
    private UUID entityId;

    @BeforeEach
    void setUp() {
        DataSource ds = createDataSource();
        runMigrations(ds);
        entityRepository = new EntityRepository(ds);
        instanceRepository = new EntityInstanceRepository(ds);
        Entity entity = entityRepository.save(Entity.builder().name("User").displayName("User").build());
        entityId = entity.id();
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
    void shouldSaveAndFindInstance() {
        UUID uuid = UUID.randomUUID();
        EntityInstance instance = EntityInstance.builder()
                .entityId(entityId)
                .uuid(uuid)
                .status(EntityInstanceStatus.ACTIVE)
                .build();

        EntityInstance saved = instanceRepository.save(instance);

        assertNotNull(saved.id());
        assertEquals(entityId, saved.entityId());
        assertEquals(uuid, saved.uuid());
        assertEquals(EntityInstanceStatus.ACTIVE, saved.status());

        Optional<EntityInstance> found = instanceRepository.findById(saved.id());
        assertTrue(found.isPresent());
        assertEquals(saved.id(), found.get().id());
    }

    @Test
    void shouldFindByUuid() {
        UUID uuid = UUID.randomUUID();
        instanceRepository.save(EntityInstance.builder().entityId(entityId).uuid(uuid).build());

        Optional<EntityInstance> found = instanceRepository.findByUuid(uuid);
        assertTrue(found.isPresent());
        assertEquals(uuid, found.get().uuid());
    }

    @Test
    void shouldFindByEntityId() {
        instanceRepository.save(EntityInstance.builder().entityId(entityId).uuid(UUID.randomUUID()).build());
        instanceRepository.save(EntityInstance.builder().entityId(entityId).uuid(UUID.randomUUID()).build());

        List<EntityInstance> instances = instanceRepository.findByEntityId(entityId);
        assertEquals(2, instances.size());
    }
}
