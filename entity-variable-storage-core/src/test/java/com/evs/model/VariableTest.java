package com.evs.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VariableTest {

    @Test
    void typeAccessorsShouldReturnCorrectValues() {
        UUID entityId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();

        Variable stringVar = Variable.builder()
                .entityId(entityId)
                .entityInstanceId(instanceId)
                .variableName("name")
                .variableType(VariableType.STRING)
                .value("test")
                .registeredAt(Instant.now())
                .build();
        assertEquals("test", stringVar.asString());
        assertNull(stringVar.asLong());

        Variable intVar = Variable.builder()
                .entityId(entityId)
                .entityInstanceId(instanceId)
                .variableName("count")
                .variableType(VariableType.INTEGER)
                .value(42L)
                .registeredAt(Instant.now())
                .build();
        assertEquals(42L, intVar.asLong());
        assertNull(intVar.asString());

        Variable jsonVar = Variable.builder()
                .entityId(entityId)
                .entityInstanceId(instanceId)
                .variableName("data")
                .variableType(VariableType.JSON)
                .value(Map.of("key", "value"))
                .registeredAt(Instant.now())
                .build();
        assertEquals(Map.of("key", "value"), jsonVar.asJson());
    }

    @Test
    void variableTypeShouldHaveColumnNames() {
        assertEquals("value_string", VariableType.STRING.getColumnName());
        assertEquals("value_int", VariableType.INTEGER.getColumnName());
        assertEquals("value_json", VariableType.JSON.getColumnName());
    }
}
