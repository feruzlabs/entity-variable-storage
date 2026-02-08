package com.evs.repository;

import com.evs.model.Entity;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class EntityRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("evs_test")
            .withUsername("test")
            .withPassword("test");

    private EntityRepository repository;

    @BeforeEach
    void setUp() {
        DataSource ds = createDataSource();
        runMigrations(ds);
        repository = new EntityRepository(ds);
    }

    private DataSource createDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(postgres.getJdbcUrl());
        ds.setUsername(postgres.getUsername());
        ds.setPassword(postgres.getPassword());
        return ds;
    }

    private void runMigrations(DataSource ds) {
        org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    @Test
    void shouldSaveAndFindEntity() {
        Entity entity = Entity.builder()
                .name("Product")
                .displayName("Product Entity")
                .build();

        Entity saved = repository.save(entity);

        assertNotNull(saved.id());
        assertEquals("Product", saved.name());

        Optional<Entity> found = repository.findById(saved.id());
        assertTrue(found.isPresent());
        assertEquals(saved.id(), found.get().id());
    }

    @Test
    void shouldFindByName() {
        Entity entity = Entity.builder()
                .name("Customer")
                .displayName("Customer")
                .build();
        repository.save(entity);

        Optional<Entity> found = repository.findByName("Customer");
        assertTrue(found.isPresent());
        assertEquals("Customer", found.get().name());
    }
}
