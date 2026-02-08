package com.evs.repository;

import com.evs.model.Variable;
import com.evs.model.VariableType;
import com.evs.util.JsonUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class VariableRepository {

    private static final int BATCH_SIZE = 1000;

    private final DataSource dataSource;

    public VariableRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Variable save(Variable variable) {
        String sql = buildInsertSql(variable.variableType());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(ps, variable);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return Variable.builder()
                            .from(variable)
                            .id(rs.getLong(1))
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to save variable", e);
        }

        throw new com.evs.exception.EVSException("Failed to save variable - no ID returned");
    }

    public List<Variable> saveBatch(List<Variable> variables) {
        if (variables.isEmpty()) {
            return List.of();
        }

        List<Variable> result = new ArrayList<>();
        Map<VariableType, List<Variable>> byType = variables.stream()
                .collect(Collectors.groupingBy(Variable::variableType));

        for (List<Variable> typeGroup : byType.values()) {
            for (int i = 0; i < typeGroup.size(); i += BATCH_SIZE) {
                List<Variable> batch = typeGroup.subList(i, Math.min(i + BATCH_SIZE, typeGroup.size()));
                result.addAll(saveBatchInternal(batch));
            }
        }

        return result;
    }

    private List<Variable> saveBatchInternal(List<Variable> variables) {
        StringBuilder sql = new StringBuilder();
        VariableType type = variables.getFirst().variableType();
        String baseSql = """
            INSERT INTO variables (
                entity_id, entity_instance_id, variable_name, variable_type,
                %s, is_indexed, is_sensitive, is_encrypted, registered_at, created_by
            ) VALUES
            """.formatted(type.getColumnName());

        sql.append(baseSql);
        for (int i = 0; i < variables.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
        sql.append(" RETURNING id");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            for (Variable v : variables) {
                setParametersAtIndex(ps, paramIdx, v);
                paramIdx += 10;
            }

            List<Variable> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                int idx = 0;
                while (rs.next()) {
                    Variable original = variables.get(idx++);
                    result.add(Variable.builder()
                            .from(original)
                            .id(rs.getLong(1))
                            .build());
                }
            }
            return result;
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to batch save variables", e);
        }
    }

    private void setParametersAtIndex(PreparedStatement ps, int startIdx, Variable variable) throws SQLException {
        ps.setObject(startIdx, variable.entityId());
        ps.setObject(startIdx + 1, variable.entityInstanceId());
        ps.setString(startIdx + 2, variable.variableName());
        ps.setString(startIdx + 3, variable.variableType().name());

        switch (variable.variableType()) {
            case STRING -> ps.setString(startIdx + 4, (String) variable.value());
            case INTEGER -> ps.setObject(startIdx + 4, variable.value() != null ? ((Number) variable.value()).longValue() : null);
            case FLOAT -> ps.setObject(startIdx + 4, variable.value() != null ? ((Number) variable.value()).doubleValue() : null);
            case BOOLEAN -> ps.setBoolean(startIdx + 4, variable.value() != null && (Boolean) variable.value());
            case JSON -> ps.setString(startIdx + 4, JsonUtil.toJson(variable.value()));
            case TIMESTAMP -> ps.setTimestamp(startIdx + 4, variable.value() != null ? Timestamp.from((Instant) variable.value()) : null);
            case BINARY -> ps.setBytes(startIdx + 4, (byte[]) variable.value());
            case UUID -> ps.setObject(startIdx + 4, variable.value());
            default -> throw new IllegalArgumentException("Unsupported variable type: " + variable.variableType());
        }
        ps.setBoolean(startIdx + 5, variable.isIndexed());
        ps.setBoolean(startIdx + 6, variable.isSensitive());
        ps.setBoolean(startIdx + 7, variable.isEncrypted());
        ps.setTimestamp(startIdx + 8, variable.registeredAt() != null ? Timestamp.from(variable.registeredAt()) : Timestamp.from(Instant.now()));
        ps.setObject(startIdx + 9, variable.createdBy());
    }

    public List<Variable> findByEntityInstanceId(UUID entityInstanceId) {
        String sql = """
            SELECT id, entity_id, entity_instance_id, variable_name, variable_type,
                   value_string, value_int, value_float, value_bool, value_json,
                   value_timestamp, value_binary, value_uuid,
                   is_indexed, is_sensitive, is_encrypted,
                   registered_at, created_at, updated_at, created_by
            FROM variables
            WHERE entity_instance_id = ?
            ORDER BY variable_name
            """;

        List<Variable> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, entityInstanceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to find variables by instance", e);
        }

        return result;
    }

    public Optional<Variable> findByInstanceIdAndName(UUID instanceId, String variableName) {
        String sql = """
            SELECT id, entity_id, entity_instance_id, variable_name, variable_type,
                   value_string, value_int, value_float, value_bool, value_json,
                   value_timestamp, value_binary, value_uuid,
                   is_indexed, is_sensitive, is_encrypted,
                   registered_at, created_at, updated_at, created_by
            FROM variables
            WHERE entity_instance_id = ? AND variable_name = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, instanceId);
            ps.setString(2, variableName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new com.evs.exception.EVSException("Failed to find variable", e);
        }

        return Optional.empty();
    }

    private String buildInsertSql(VariableType type) {
        return String.format("""
            INSERT INTO variables (
                entity_id, entity_instance_id, variable_name, variable_type,
                %s, is_indexed, is_sensitive, is_encrypted, registered_at, created_by
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, type.getColumnName());
    }

    private void setParameters(PreparedStatement ps, Variable variable) throws SQLException {
        ps.setObject(1, variable.entityId());
        ps.setObject(2, variable.entityInstanceId());
        ps.setString(3, variable.variableName());
        ps.setString(4, variable.variableType().name());

        int paramIdx = 5;
        switch (variable.variableType()) {
            case STRING -> ps.setString(paramIdx, (String) variable.value());
            case INTEGER -> ps.setObject(paramIdx, variable.value() != null ? ((Number) variable.value()).longValue() : null);
            case FLOAT -> ps.setObject(paramIdx, variable.value() != null ? ((Number) variable.value()).doubleValue() : null);
            case BOOLEAN -> ps.setObject(paramIdx, variable.value());
            case JSON -> ps.setString(paramIdx, JsonUtil.toJson(variable.value()));
            case TIMESTAMP -> ps.setTimestamp(paramIdx, variable.value() != null ? Timestamp.from((Instant) variable.value()) : null);
            case BINARY -> ps.setBytes(paramIdx, (byte[]) variable.value());
            case UUID -> ps.setObject(paramIdx, variable.value());
            default -> throw new IllegalArgumentException("Unsupported variable type: " + variable.variableType());
        }
        paramIdx++;
        ps.setBoolean(paramIdx++, variable.isIndexed());
        ps.setBoolean(paramIdx++, variable.isSensitive());
        ps.setBoolean(paramIdx++, variable.isEncrypted());
        ps.setTimestamp(paramIdx++, variable.registeredAt() != null ? Timestamp.from(variable.registeredAt()) : Timestamp.from(Instant.now()));
        ps.setObject(paramIdx, variable.createdBy());
    }

    private Variable mapRow(ResultSet rs) throws SQLException {
        VariableType type = VariableType.valueOf(rs.getString("variable_type"));
        Object value = getValueByType(rs, type);

        return Variable.builder()
                .id(rs.getLong("id"))
                .entityId((UUID) rs.getObject("entity_id"))
                .entityInstanceId((UUID) rs.getObject("entity_instance_id"))
                .variableName(rs.getString("variable_name"))
                .variableType(type)
                .value(value)
                .isIndexed(rs.getBoolean("is_indexed"))
                .isSensitive(rs.getBoolean("is_sensitive"))
                .isEncrypted(rs.getBoolean("is_encrypted"))
                .registeredAt(toInstant(rs.getTimestamp("registered_at")))
                .createdAt(toInstant(rs.getTimestamp("created_at")))
                .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                .createdBy((UUID) rs.getObject("created_by"))
                .build();
    }

    private Object getValueByType(ResultSet rs, VariableType type) throws SQLException {
        return switch (type) {
            case STRING -> rs.getString("value_string");
            case INTEGER -> {
                long v = rs.getLong("value_int");
                yield rs.wasNull() ? null : v;
            }
            case FLOAT -> {
                double v = rs.getDouble("value_float");
                yield rs.wasNull() ? null : v;
            }
            case BOOLEAN -> {
                boolean v = rs.getBoolean("value_bool");
                yield rs.wasNull() ? null : v;
            }
            case JSON -> JsonUtil.fromJson(rs.getString("value_json"));
            case TIMESTAMP -> toInstant(rs.getTimestamp("value_timestamp"));
            case BINARY -> rs.getBytes("value_binary");
            case UUID -> rs.getObject("value_uuid", UUID.class);
        };
    }

    private static Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }
}
