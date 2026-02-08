package com.example;

import com.evs.model.Entity;
import com.evs.model.EntityInstance;
import com.evs.model.Variable;
import com.evs.model.VariableType;
import com.evs.service.EntityInstanceService;
import com.evs.service.EntityService;
import com.evs.service.VariableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/evs")
@Tag(name = "EVS Demo", description = "Entity Variable Storage API - tekshirish uchun")
public class EVSDemoController {

    private final EntityService entityService;
    private final EntityInstanceService instanceService;
    private final VariableService variableService;

    public EVSDemoController(EntityService entityService,
                             EntityInstanceService instanceService,
                             VariableService variableService) {
        this.entityService = entityService;
        this.instanceService = instanceService;
        this.variableService = variableService;
    }

    @Operation(summary = "Entity yaratish")
    @PostMapping("/entities")
    public ResponseEntity<Entity> createEntity(@RequestBody CreateEntityRequest request) {
        Entity entity = Entity.builder()
                .name(request.name())
                .displayName(request.displayName())
                .description(request.description())
                .build();
        entity = entityService.createEntity(entity);
        return ResponseEntity.ok(entity);
    }

    @Operation(summary = "Barcha entitylarni olish")
    @GetMapping("/entities")
    public ResponseEntity<List<Entity>> listEntities() {
        return ResponseEntity.ok(entityService.findAll());
    }

    @Operation(summary = "Entity instance yaratish")
    @PostMapping("/entities/{entityId}/instances")
    public ResponseEntity<EntityInstance> createInstance(@PathVariable UUID entityId,
                                                         @RequestBody CreateInstanceRequest request) {
        EntityInstance instance = EntityInstance.builder()
                .entityId(entityId)
                .uuid(request.uuid() != null ? request.uuid() : UUID.randomUUID())
                .build();
        instance = instanceService.createInstance(instance);
        return ResponseEntity.ok(instance);
    }

    @Operation(summary = "Variablelar yaratish")
    @PostMapping("/instances/{instanceId}/variables")
    public ResponseEntity<List<Variable>> createVariables(@PathVariable UUID instanceId,
                                                          @RequestBody List<CreateVariableRequest> request) {
        EntityInstance instance = instanceService.getInstance(instanceId);
        List<Variable> variables = request.stream()
                .map(r -> Variable.builder()
                        .entityId(instance.entityId())
                        .entityInstanceId(instanceId)
                        .variableName(r.name())
                        .variableType(r.type())
                        .value(r.value())
                        .registeredAt(instance.registeredAt())
                        .build())
                .toList();
        variables = variableService.createVariablesBatch(variables);
        return ResponseEntity.ok(variables);
    }

    @Operation(summary = "Instance variablelarini olish")
    @GetMapping("/instances/{instanceId}/variables")
    public ResponseEntity<Map<String, Object>> getVariables(@PathVariable UUID instanceId) {
        Map<String, Object> vars = variableService.getVariablesAsMap(instanceId);
        return ResponseEntity.ok(vars);
    }

    public record CreateEntityRequest(String name, String displayName, String description) {}
    public record CreateInstanceRequest(UUID uuid) {}
    public record CreateVariableRequest(String name, VariableType type, Object value) {}
}
