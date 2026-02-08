package com.evs.service;

import com.evs.exception.EVSException;
import com.evs.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class EntityServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("evs_test")
            .withUsername("test")
            .withPassword("test");

    private EntityService entityService;

    @BeforeEach
    void setUp() {
        com.evs.config.EVSConfig config = new com.evs.config.EVSConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(5);

        com.evs.config.EVSFactory factory = new com.evs.config.EVSFactory(config);
        entityService = factory.entityService();
    }

    @Test
    void shouldCreateAndFindEntity() {
        Entity entity = entityService.createEntity(
                Entity.builder().name("Product").displayName("Product").description("Test").build()
        );

        assertNotNull(entity.id());
        assertEquals("Product", entity.name());

        Entity found = entityService.getEntity(entity.id());
        assertEquals(entity.id(), found.id());
    }

    @Test
    void shouldThrowWhenEntityNotFound() {
        assertThrows(EVSException.class, () ->
                entityService.getEntity(UUID.randomUUID())
        );
    }

    @Test
    void shouldThrowWhenEntityNameNotFound() {
        assertThrows(EVSException.class, () ->
                entityService.getEntityByName("NonExistent")
        );
    }

    @Test
    void shouldFindAllEntities() {
        entityService.createEntity(Entity.builder().name("A").displayName("A").build());
        entityService.createEntity(Entity.builder().name("B").displayName("B").build());

        List<Entity> all = entityService.findAll();
        assertTrue(all.size() >= 2);
    }
}
