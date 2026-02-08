package com.evs.service;

import com.evs.exception.EVSException;
import com.evs.model.EntityInstance;
import com.evs.repository.EntityInstanceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for EntityInstance management.
 */
public class EntityInstanceService {

    private final EntityInstanceRepository entityInstanceRepository;

    public EntityInstanceService(EntityInstanceRepository entityInstanceRepository) {
        this.entityInstanceRepository = entityInstanceRepository;
    }

    public EntityInstance createInstance(EntityInstance instance) {
        return entityInstanceRepository.save(instance);
    }

    public EntityInstance getInstance(UUID id) {
        return entityInstanceRepository.findById(id)
                .orElseThrow(() -> new EVSException("Entity instance not found: " + id));
    }

    public EntityInstance getInstanceByUuid(UUID uuid) {
        return entityInstanceRepository.findByUuid(uuid)
                .orElseThrow(() -> new EVSException("Entity instance not found for uuid: " + uuid));
    }

    public Optional<EntityInstance> findById(UUID id) {
        return entityInstanceRepository.findById(id);
    }

    public Optional<EntityInstance> findByUuid(UUID uuid) {
        return entityInstanceRepository.findByUuid(uuid);
    }

    public List<EntityInstance> findByEntityId(UUID entityId) {
        return entityInstanceRepository.findByEntityId(entityId);
    }
}
