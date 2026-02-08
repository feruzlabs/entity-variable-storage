package com.evs.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an instance of an entity with a unique identifier.
 */
public record EntityInstance(
        UUID id,
        UUID entityId,
        UUID uuid,
        EntityInstanceStatus status,
        Instant registeredAt,
        Instant expiresAt,
        Map<String, Object> context,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID entityId;
        private UUID uuid;
        private EntityInstanceStatus status = EntityInstanceStatus.ACTIVE;
        private Instant registeredAt;
        private Instant expiresAt;
        private Map<String, Object> context;
        private Map<String, Object> metadata;
        private Instant createdAt;
        private Instant updatedAt;
        private UUID createdBy;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder entityId(UUID entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder status(EntityInstanceStatus status) {
            this.status = status;
            return this;
        }

        public Builder registeredAt(Instant registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder context(Map<String, Object> context) {
            this.context = context != null ? Map.copyOf(context) : null;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? Map.copyOf(metadata) : null;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder createdBy(UUID createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public EntityInstance build() {
            return new EntityInstance(
                    id,
                    entityId,
                    uuid,
                    status,
                    registeredAt != null ? registeredAt : Instant.now(),
                    expiresAt,
                    context != null ? context : Collections.emptyMap(),
                    metadata != null ? metadata : Collections.emptyMap(),
                    createdAt,
                    updatedAt,
                    createdBy
            );
        }
    }
}
