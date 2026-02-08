package com.example;

import com.evs.config.EVSConfig;
import com.evs.config.EVSFactory;
import com.evs.model.Entity;
import com.evs.model.EntityInstance;
import com.evs.model.Variable;
import com.evs.model.VariableType;
import com.evs.service.EntityInstanceService;
import com.evs.service.EntityService;
import com.evs.service.VariableService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Standalone usage example - no framework required.
 */
public class StandaloneExample {

    public static void main(String[] args) {
        // Configure
        EVSConfig config = new EVSConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/evs_db");
        config.setUsername("postgres");
        config.setPassword("postgres");

        // Initialize
        EVSFactory factory = new EVSFactory(config);
        EntityService entityService = factory.entityService();
        EntityInstanceService instanceService = factory.entityInstanceService();
        VariableService variableService = factory.variableService();

        // 1. Create entity
        Entity userEntity = Entity.builder()
                .name("User")
                .displayName("User Entity")
                .description("User domain model")
                .build();
        userEntity = entityService.createEntity(userEntity);
        System.out.println("Created entity: " + userEntity.name() + " with id: " + userEntity.id());

        // 2. Create instance
        EntityInstance instance = EntityInstance.builder()
                .entityId(userEntity.id())
                .uuid(UUID.randomUUID())
                .build();
        instance = instanceService.createInstance(instance);
        System.out.println("Created instance: " + instance.id());

        // 3. Create variables
        List<Variable> variables = List.of(
                Variable.builder()
                        .entityId(userEntity.id())
                        .entityInstanceId(instance.id())
                        .variableName("name")
                        .variableType(VariableType.STRING)
                        .value("John Doe")
                        .registeredAt(instance.registeredAt())
                        .build(),

                Variable.builder()
                        .entityId(userEntity.id())
                        .entityInstanceId(instance.id())
                        .variableName("age")
                        .variableType(VariableType.INTEGER)
                        .value(30L)
                        .registeredAt(instance.registeredAt())
                        .build(),

                Variable.builder()
                        .entityId(userEntity.id())
                        .entityInstanceId(instance.id())
                        .variableName("metadata")
                        .variableType(VariableType.JSON)
                        .value(Map.of("department", "Engineering"))
                        .registeredAt(instance.registeredAt())
                        .build()
        );

        variableService.createVariablesBatch(variables);
        System.out.println("Created " + variables.size() + " variables");

        // 4. Query variables
        Map<String, Object> vars = variableService.getVariablesAsMap(instance.id());
        System.out.println("Variables: " + vars);
    }
}
