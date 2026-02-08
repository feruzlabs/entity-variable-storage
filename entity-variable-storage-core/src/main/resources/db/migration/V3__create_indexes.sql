-- Additional indexes for common query patterns
-- Note: Indexes on the variables partitioned table are inherited by child partitions
-- when they are created. The base indexes are defined in V1.

-- Index for partition metadata lookups by partition type
CREATE INDEX IF NOT EXISTS idx_partition_meta_type ON partition_metadata(partition_type);
