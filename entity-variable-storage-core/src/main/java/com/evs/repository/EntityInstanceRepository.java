package com.evs.repository;

import com.evs.model.EntityInstance;
import com.evs.model.EntityInstanceStatus;
import com.evs.util.JsonUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityInstanceRepository {

    private final DataSource dataSource;

    public EntityInstanceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EntityInstance save(EntityInstance instance) {
        String sql = """
            INSERT INTO entity_instances (id, entity_id, uuid, status, registered_at, expires_at,
                                         context, metadata, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)
            RETURNING id, registered_at, created_at, updated_at
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            UUID id = instance.id() != null ? instance.id() : UUID.randomUUID();
            ps.setObject(1, id);
            ps.setObject(2, instance.entityId());
            ps.setObject(3, instance.uuid());
            ps.setString(4, instance.status().name());
            ps.setTimestamp(5, instance.registeredAt() != null ? Timestamp.from(instance.registeredAt()) : Timestamp.from(Instant.now()));
            ps.setObject(6, instance.expiresAt() != null ? Timestamp.from(instance.expiresAt()) : null);
            ps.setString(7, JsonUtil.toJson(instance.context()));
            ps.setString(8, JsonUtil.toJson(instance.metadata()));
            ps.setObject(9, instance.createdBy());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Instant registeredAt = toInstant(rs.getTimestamp("registered_at"));
                    Instant createdAt = toInstant(rs.getTimestamp("created_at"));
                    Instant updatedAt = toInstant(rs.getTimestamp("updated_at"));
                    return EntityInstance.builder()
                            .id(id)
                            .entityId(instance.entityId())
                            .uuid(instance.uuid())
                            .status(instance.status())
                            .registeredAt(registeredAt)
                            .expiresAt(instance.expiresAt())
                            .context(instance.context())
                            .metadata(instance.metadata())
                            .createdAt(createdAt)
                            .updatedAt(updatedAt)
                            .createdBy(instance.createdBy())
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to save entity instance", e);
        }

        throw new com.evs.exception.EVSException("Failed to save entity instance");
    }

    public Optional<EntityInstance> findById(UUID id) {
        String sql = """
            SELECT id, entity_id, uuid, status, registered_at, expires_at, context, metadata,
                   created_at, updated_at, created_by
            FROM entity_instances WHERE id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to find entity instance", e);
        }

        return Optional.empty();
    }

    public Optional<EntityInstance> findByUuid(UUID uuid) {
        String sql = """
            SELECT id, entity_id, uuid, status, registered_at, expires_at, context, metadata,
                   created_at, updated_at, created_by
            FROM entity_instances WHERE uuid = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, uuid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to find entity instance by uuid", e);
        }

        return Optional.empty();
    }

    public List<EntityInstance> findByEntityId(UUID entityId) {
        String sql = """
            SELECT id, entity_id, uuid, status, registered_at, expires_at, context, metadata,
                   created_at, updated_at, created_by
            FROM entity_instances WHERE entity_id = ? ORDER BY registered_at DESC
            """;

        List<EntityInstance> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, entityId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to find entity instances", e);
        }

        return result;
    }

    private EntityInstance mapRow(ResultSet rs) throws SQLException {
        return EntityInstance.builder()
                .id((UUID) rs.getObject("id"))
                .entityId((UUID) rs.getObject("entity_id"))
                .uuid((UUID) rs.getObject("uuid"))
                .status(EntityInstanceStatus.valueOf(rs.getString("status")))
                .registeredAt(toInstant(rs.getTimestamp("registered_at")))
                .expiresAt(toInstant(rs.getTimestamp("expires_at")))
                .context(JsonUtil.fromJson(rs.getString("context")))
                .metadata(JsonUtil.fromJson(rs.getString("metadata")))
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                .createdBy((UUID) rs.getObject("created_by"))
                .build();
    }

    private static Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }
}
