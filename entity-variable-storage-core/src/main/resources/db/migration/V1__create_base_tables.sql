-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Entities table
CREATE TABLE entities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(500),
    description TEXT,
    schema_definition JSONB,
    metadata JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID
);

CREATE INDEX idx_entities_name ON entities(name);
CREATE INDEX idx_entities_active ON entities(is_active);

-- Entity instances table
CREATE TABLE entity_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id UUID NOT NULL REFERENCES entities(id) ON DELETE CASCADE,
    uuid UUID UNIQUE NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ,
    context JSONB,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID
);

CREATE INDEX idx_entity_instances_entity_id ON entity_instances(entity_id);
CREATE INDEX idx_entity_instances_uuid ON entity_instances(uuid);
CREATE INDEX idx_entity_instances_status ON entity_instances(status);
CREATE INDEX idx_entity_instances_entity_registered ON entity_instances(entity_id, registered_at);

-- Variables table (PARTITIONED)
CREATE TABLE variables (
    id BIGSERIAL,
    entity_id UUID NOT NULL,
    entity_instance_id UUID NOT NULL REFERENCES entity_instances(id) ON DELETE CASCADE,
    variable_name VARCHAR(255) NOT NULL,
    variable_type VARCHAR(50) NOT NULL,

    -- Type-specific value columns
    value_string TEXT,
    value_int BIGINT,
    value_float DOUBLE PRECISION,
    value_bool BOOLEAN,
    value_json JSONB,
    value_timestamp TIMESTAMPTZ,
    value_binary BYTEA,
    value_uuid UUID,

    -- Metadata
    is_indexed BOOLEAN DEFAULT false,
    is_sensitive BOOLEAN DEFAULT false,
    is_encrypted BOOLEAN DEFAULT false,

    registered_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,

    PRIMARY KEY (entity_id, id, registered_at)
) PARTITION BY LIST (entity_id);

CREATE INDEX idx_variables_instance_name ON variables(entity_instance_id, variable_name);
CREATE INDEX idx_variables_name ON variables(variable_name);
CREATE INDEX idx_variables_json ON variables USING GIN(value_json);

-- Partition metadata tracking
CREATE TABLE partition_metadata (
    id SERIAL PRIMARY KEY,
    entity_id UUID REFERENCES entities(id),
    entity_name VARCHAR(255) NOT NULL,
    partition_name VARCHAR(255) UNIQUE NOT NULL,
    partition_type VARCHAR(50) NOT NULL,
    partition_start DATE,
    partition_end DATE,
    row_count BIGINT DEFAULT 0,
    size_bytes BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_analyzed_at TIMESTAMPTZ
);

CREATE INDEX idx_partition_meta_entity ON partition_metadata(entity_id);
