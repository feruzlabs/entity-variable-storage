package com.evs.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an entity class/schema in the EVS system.
 */
public record Entity(
        UUID id,
        String name,
        String displayName,
        String description,
        Map<String, Object> schemaDefinition,
        Map<String, Object> metadata,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String displayName;
        private String description;
        private Map<String, Object> schemaDefinition;
        private Map<String, Object> metadata;
        private boolean isActive = true;
        private Instant createdAt;
        private Instant updatedAt;
        private UUID createdBy;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder schemaDefinition(Map<String, Object> schemaDefinition) {
            this.schemaDefinition = schemaDefinition != null ? Map.copyOf(schemaDefinition) : null;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? Map.copyOf(metadata) : null;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
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

        public Entity build() {
            return new Entity(
                    id,
                    name,
                    displayName,
                    description,
                    schemaDefinition != null ? schemaDefinition : Collections.emptyMap(),
                    metadata != null ? metadata : Collections.emptyMap(),
                    isActive,
                    createdAt,
                    updatedAt,
                    createdBy
            );
        }
    }
}
