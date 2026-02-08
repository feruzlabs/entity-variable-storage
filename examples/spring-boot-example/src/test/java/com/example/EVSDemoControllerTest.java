package com.example;

import com.evs.model.Entity;
import com.evs.model.EntityInstance;
import com.evs.model.Variable;
import com.evs.model.VariableType;
import com.evs.service.EntityInstanceService;
import com.evs.service.EntityService;
import com.evs.service.VariableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.evs.springboot.autoconfigure.EVSAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EVSDemoController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class, EVSAutoConfiguration.class})
class EVSDemoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EntityService entityService;

    @MockBean
    EntityInstanceService instanceService;

    @MockBean
    VariableService variableService;

    @Test
    void shouldCreateEntity() throws Exception {
        UUID entityId = UUID.randomUUID();
        Entity entity = Entity.builder()
                .id(entityId)
                .name("TestEntity")
                .displayName("Test")
                .description("Desc")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(entityService.createEntity(any())).thenReturn(entity);

        mockMvc.perform(post("/api/evs/entities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"TestEntity","displayName":"Test","description":"Desc"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entityId.toString()))
                .andExpect(jsonPath("$.name").value("TestEntity"));
    }

    @Test
    void shouldListEntities() throws Exception {
        Entity entity = Entity.builder()
                .id(UUID.randomUUID())
                .name("User")
                .displayName("User")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(entityService.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/evs/entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("User"));
    }

    @Test
    void shouldCreateInstance() throws Exception {
        UUID entityId = UUID.randomUUID();
        UUID instanceId = UUID.randomUUID();
        EntityInstance instance = EntityInstance.builder()
                .id(instanceId)
                .entityId(entityId)
                .uuid(UUID.randomUUID())
                .registeredAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(instanceService.createInstance(any())).thenReturn(instance);

        mockMvc.perform(post("/api/evs/entities/{entityId}/instances", entityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(instanceId.toString()));
    }

    @Test
    void shouldCreateVariablesAndGetVariables() throws Exception {
        UUID instanceId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        EntityInstance instance = EntityInstance.builder()
                .id(instanceId)
                .entityId(entityId)
                .uuid(UUID.randomUUID())
                .registeredAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Variable v1 = Variable.builder()
                .id(1L)
                .entityId(entityId)
                .entityInstanceId(instanceId)
                .variableName("email")
                .variableType(VariableType.STRING)
                .value("test@example.com")
                .registeredAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Variable v2 = Variable.builder()
                .id(2L)
                .entityId(entityId)
                .entityInstanceId(instanceId)
                .variableName("age")
                .variableType(VariableType.INTEGER)
                .value(25L)
                .registeredAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(instanceService.getInstance(instanceId)).thenReturn(instance);
        when(variableService.createVariablesBatch(any())).thenReturn(List.of(v1, v2));
        when(variableService.getVariablesAsMap(instanceId)).thenReturn(Map.of("email", "test@example.com", "age", 25));

        mockMvc.perform(post("/api/evs/instances/{instanceId}/variables", instanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            [
                              {"name":"email","type":"STRING","value":"test@example.com"},
                              {"name":"age","type":"INTEGER","value":25}
                            ]
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));

        mockMvc.perform(get("/api/evs/instances/{instanceId}/variables", instanceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }
}
