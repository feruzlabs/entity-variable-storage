-- Function to create entity-level partition
CREATE OR REPLACE FUNCTION create_entity_partition(
    p_entity_id UUID,
    p_entity_name VARCHAR
) RETURNS VARCHAR AS $$
DECLARE
    partition_name VARCHAR;
BEGIN
    partition_name := 'variables_entity_' || lower(regexp_replace(p_entity_name, '[^a-zA-Z0-9]', '_', 'g'));

    -- Create entity-level partition
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF variables FOR VALUES IN (%L) PARTITION BY RANGE (registered_at)',
        partition_name,
        p_entity_id
    );

    -- Track in metadata
    INSERT INTO partition_metadata (entity_id, entity_name, partition_name, partition_type, is_active)
    VALUES (p_entity_id, p_entity_name, partition_name, 'ENTITY_LEVEL', true)
    ON CONFLICT (partition_name) DO NOTHING;

    RETURN partition_name;
END;
$$ LANGUAGE plpgsql;

-- Function to create monthly sub-partition
CREATE OR REPLACE FUNCTION create_monthly_partition(
    p_entity_id UUID,
    p_entity_name VARCHAR,
    p_year INT,
    p_month INT
) RETURNS VARCHAR AS $$
DECLARE
    entity_partition_name VARCHAR;
    monthly_partition_name VARCHAR;
    start_date DATE;
    end_date DATE;
BEGIN
    entity_partition_name := 'variables_entity_' || lower(regexp_replace(p_entity_name, '[^a-zA-Z0-9]', '_', 'g'));
    monthly_partition_name := entity_partition_name || '_y' || p_year || 'm' || lpad(p_month::TEXT, 2, '0');

    start_date := make_date(p_year, p_month, 1);
    end_date := start_date + INTERVAL '1 month';

    -- Create monthly partition
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
        monthly_partition_name,
        entity_partition_name,
        start_date,
        end_date
    );

    -- Track in metadata
    INSERT INTO partition_metadata (
        entity_id, entity_name, partition_name, partition_type,
        partition_start, partition_end, is_active
    )
    VALUES (
        p_entity_id, p_entity_name, monthly_partition_name, 'MONTH_LEVEL',
        start_date, end_date, true
    )
    ON CONFLICT (partition_name) DO NOTHING;

    RETURN monthly_partition_name;
END;
$$ LANGUAGE plpgsql;

-- Function to auto-create next 3 months partitions
CREATE OR REPLACE FUNCTION auto_create_partitions(
    p_entity_id UUID,
    p_entity_name VARCHAR
) RETURNS VOID AS $$
DECLARE
    current_month INT;
    current_year INT;
    i INT;
    target_month INT;
    target_year INT;
BEGIN
    current_month := EXTRACT(MONTH FROM CURRENT_DATE);
    current_year := EXTRACT(YEAR FROM CURRENT_DATE);

    FOR i IN 0..2 LOOP
        target_month := current_month + i;
        target_year := current_year;

        -- Handle year rollover
        IF target_month > 12 THEN
            target_month := target_month - 12;
            target_year := target_year + 1;
        END IF;

        PERFORM create_monthly_partition(p_entity_id, p_entity_name, target_year, target_month);
    END LOOP;
END;
$$ LANGUAGE plpgsql;
