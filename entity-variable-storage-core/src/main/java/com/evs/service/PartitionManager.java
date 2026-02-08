package com.evs.service;

import com.evs.exception.EVSException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Manages PostgreSQL partitions for the variables table.
 */
public class PartitionManager {

    private final DataSource dataSource;

    public PartitionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createEntityPartition(UUID entityId, String entityName) {
        String sql = "SELECT create_entity_partition(?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, entityId);
            ps.setString(2, entityName);
            ps.executeQuery();

            autoCreatePartitions(entityId, entityName);

        } catch (SQLException e) {
            throw new EVSException("Failed to create partition for entity: " + entityName, e);
        }
    }

    public void autoCreatePartitions(UUID entityId, String entityName) {
        String sql = "SELECT auto_create_partitions(?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, entityId);
            ps.setString(2, entityName);
            ps.executeQuery();

        } catch (SQLException e) {
            throw new EVSException("Failed to auto-create partitions for entity: " + entityName, e);
        }
    }
}
