package com.evs.service;

import com.evs.exception.EVSException;
import com.evs.model.Entity;
import com.evs.repository.EntityRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for Entity management.
 */
public class EntityService {

    private final EntityRepository entityRepository;
    private final PartitionManager partitionManager;

    public EntityService(EntityRepository entityRepository, PartitionManager partitionManager) {
        this.entityRepository = entityRepository;
        this.partitionManager = partitionManager;
    }

    public Entity createEntity(Entity entity) {
        Entity saved = entityRepository.save(entity);
        partitionManager.createEntityPartition(saved.id(), saved.name());
        return saved;
    }

    public Entity getEntity(UUID id) {
        return entityRepository.findById(id)
                .orElseThrow(() -> new EVSException("Entity not found: " + id));
    }

    public Entity getEntityByName(String name) {
        return entityRepository.findByName(name)
                .orElseThrow(() -> new EVSException("Entity not found: " + name));
    }

    public Optional<Entity> findById(UUID id) {
        return entityRepository.findById(id);
    }

    public Optional<Entity> findByName(String name) {
        return entityRepository.findByName(name);
    }

    public List<Entity> findAll() {
        return entityRepository.findAll();
    }
}
