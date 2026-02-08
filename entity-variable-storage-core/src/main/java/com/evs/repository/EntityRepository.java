package com.evs.repository;

import com.evs.model.Entity;
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

public class EntityRepository {

    private final DataSource dataSource;

    public EntityRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Entity save(Entity entity) {
        String sql = """
            INSERT INTO entities (id, name, display_name, description, schema_definition,
                                 metadata, is_active, created_by)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?)
            RETURNING id, created_at, updated_at
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            UUID id = entity.id() != null ? entity.id() : UUID.randomUUID();
            ps.setObject(1, id);
            ps.setString(2, entity.name());
            ps.setString(3, entity.displayName());
            ps.setString(4, entity.description());
            ps.setString(5, JsonUtil.toJson(entity.schemaDefinition()));
            ps.setString(6, JsonUtil.toJson(entity.metadata()));
            ps.setBoolean(7, entity.isActive());
            ps.setObject(8, entity.createdBy());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Instant createdAt = toInstant(rs.getTimestamp("created_at"));
                    Instant updatedAt = toInstant(rs.getTimestamp("updated_at"));
                    return Entity.builder()
                            .id(id)
                            .name(entity.name())
                            .displayName(entity.displayName())
                            .description(entity.description())
                            .schemaDefinition(entity.schemaDefinition())
                            .metadata(entity.metadata())
                            .isActive(entity.isActive())
                            .createdAt(createdAt)
                            .updatedAt(updatedAt)
                            .createdBy(entity.createdBy())
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to save entity", e);
        }

        throw new com.evs.exception.EVSException("Failed to save entity");
    }

    public Optional<Entity> findById(UUID id) {
        String sql = """
            SELECT id, name, display_name, description, schema_definition, metadata,
                   is_active, created_at, updated_at, created_by
            FROM entities WHERE id = ?
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
            throw new com.evs.exception.EVSException("Failed to find entity by id", e);
        }

        return Optional.empty();
    }

    public Optional<Entity> findByName(String name) {
        String sql = """
            SELECT id, name, display_name, description, schema_definition, metadata,
                   is_active, created_at, updated_at, created_by
            FROM entities WHERE name = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to find entity by name", e);
        }

        return Optional.empty();
    }

    public List<Entity> findAll() {
        String sql = """
            SELECT id, name, display_name, description, schema_definition, metadata,
                   is_active, created_at, updated_at, created_by
            FROM entities ORDER BY name
            """;

        List<Entity> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to find all entities", e);
        }

        return result;
    }

    private Entity mapRow(ResultSet rs) throws SQLException {
        return Entity.builder()
                .id((UUID) rs.getObject("id"))
                .name(rs.getString("name"))
                .displayName(rs.getString("display_name"))
                .description(rs.getString("description"))
                .schemaDefinition(JsonUtil.fromJson(rs.getString("schema_definition")))
                .metadata(JsonUtil.fromJson(rs.getString("metadata")))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                .createdBy((UUID) rs.getObject("created_by"))
                .build();
    }

    private static Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }
}
