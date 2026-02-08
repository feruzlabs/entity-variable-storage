package com.evs.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a variable (property) of an entity instance.
 * Stored in partitioned tables for optimal performance.
 */
public record Variable(
        Long id,
        UUID entityId,
        UUID entityInstanceId,
        String variableName,
        VariableType variableType,
        Object value,
        boolean isIndexed,
        boolean isSensitive,
        boolean isEncrypted,
        Instant registeredAt,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy) {

    public String asString() {
        return variableType == VariableType.STRING ? (String) value : null;
    }

    public Long asLong() {
        return variableType == VariableType.INTEGER ? (Long) value : null;
    }

    public Double asDouble() {
        return variableType == VariableType.FLOAT ? (Double) value : null;
    }

    public Boolean asBoolean() {
        return variableType == VariableType.BOOLEAN ? (Boolean) value : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> asJson() {
        return variableType == VariableType.JSON ? (Map<String, Object>) value : null;
    }

    public java.time.Instant asTimestamp() {
        return variableType == VariableType.TIMESTAMP ? (Instant) value : null;
    }

    public byte[] asBinary() {
        return variableType == VariableType.BINARY ? (byte[]) value : null;
    }

    public UUID asUuid() {
        return variableType == VariableType.UUID ? (UUID) value : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private UUID entityId;
        private UUID entityInstanceId;
        private String variableName;
        private VariableType variableType;
        private Object value;
        private boolean isIndexed = false;
        private boolean isSensitive = false;
        private boolean isEncrypted = false;
        private Instant registeredAt;
        private Instant createdAt;
        private Instant updatedAt;
        private UUID createdBy;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder entityId(UUID entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder entityInstanceId(UUID entityInstanceId) {
            this.entityInstanceId = entityInstanceId;
            return this;
        }

        public Builder variableName(String variableName) {
            this.variableName = variableName;
            return this;
        }

        public Builder variableType(VariableType variableType) {
            this.variableType = variableType;
            return this;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public Builder isIndexed(boolean isIndexed) {
            this.isIndexed = isIndexed;
            return this;
        }

        public Builder isSensitive(boolean isSensitive) {
            this.isSensitive = isSensitive;
            return this;
        }

        public Builder isEncrypted(boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
            return this;
        }

        public Builder registeredAt(Instant registeredAt) {
            this.registeredAt = registeredAt;
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

        public Builder from(Variable variable) {
            this.id = variable.id();
            this.entityId = variable.entityId();
            this.entityInstanceId = variable.entityInstanceId();
            this.variableName = variable.variableName();
            this.variableType = variable.variableType();
            this.value = variable.value();
            this.isIndexed = variable.isIndexed();
            this.isSensitive = variable.isSensitive();
            this.isEncrypted = variable.isEncrypted();
            this.registeredAt = variable.registeredAt();
            this.createdAt = variable.createdAt();
            this.updatedAt = variable.updatedAt();
            this.createdBy = variable.createdBy();
            return this;
        }

        public Variable build() {
            return new Variable(
                    id,
                    entityId,
                    entityInstanceId,
                    variableName,
                    variableType,
                    value,
                    isIndexed,
                    isSensitive,
                    isEncrypted,
                    registeredAt,
                    createdAt,
                    updatedAt,
                    createdBy
            );
        }
    }
}
