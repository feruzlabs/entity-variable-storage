package com.evs.model;

/**
 * Supported variable value types with corresponding database column names.
 */
public enum VariableType {
    STRING("value_string"),
    INTEGER("value_int"),
    FLOAT("value_float"),
    BOOLEAN("value_bool"),
    JSON("value_json"),
    TIMESTAMP("value_timestamp"),
    BINARY("value_binary"),
    UUID("value_uuid");

    private final String columnName;

    VariableType(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
