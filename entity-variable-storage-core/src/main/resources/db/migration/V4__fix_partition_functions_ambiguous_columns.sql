-- Fix ambiguous column reference: rename PL/pgSQL variables to avoid conflict with partition_metadata columns

CREATE OR REPLACE FUNCTION create_entity_partition(
    p_entity_id UUID,
    p_entity_name VARCHAR
) RETURNS VARCHAR AS $$
DECLARE
    v_partition_name VARCHAR;
BEGIN
    v_partition_name := 'variables_entity_' || lower(regexp_replace(p_entity_name, '[^a-zA-Z0-9]', '_', 'g'));

    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF variables FOR VALUES IN (%L) PARTITION BY RANGE (registered_at)',
        v_partition_name,
        p_entity_id
    );

    INSERT INTO partition_metadata (entity_id, entity_name, partition_name, partition_type, is_active)
    VALUES (p_entity_id, p_entity_name, v_partition_name, 'ENTITY_LEVEL', true)
    ON CONFLICT (partition_name) DO NOTHING;

    RETURN v_partition_name;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_monthly_partition(
    p_entity_id UUID,
    p_entity_name VARCHAR,
    p_year INT,
    p_month INT
) RETURNS VARCHAR AS $$
DECLARE
    v_entity_partition_name VARCHAR;
    v_monthly_partition_name VARCHAR;
    v_start_date DATE;
    v_end_date DATE;
BEGIN
    v_entity_partition_name := 'variables_entity_' || lower(regexp_replace(p_entity_name, '[^a-zA-Z0-9]', '_', 'g'));
    v_monthly_partition_name := v_entity_partition_name || '_y' || p_year || 'm' || lpad(p_month::TEXT, 2, '0');

    v_start_date := make_date(p_year, p_month, 1);
    v_end_date := v_start_date + INTERVAL '1 month';

    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
        v_monthly_partition_name,
        v_entity_partition_name,
        v_start_date,
        v_end_date
    );

    INSERT INTO partition_metadata (
        entity_id, entity_name, partition_name, partition_type,
        partition_start, partition_end, is_active
    )
    VALUES (
        p_entity_id, p_entity_name, v_monthly_partition_name, 'MONTH_LEVEL',
        v_start_date, v_end_date, true
    )
    ON CONFLICT (partition_name) DO NOTHING;

    RETURN v_monthly_partition_name;
END;
$$ LANGUAGE plpgsql;
