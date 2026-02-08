package com.evs.service;

import com.evs.model.Variable;
import com.evs.model.VariableType;
import com.evs.repository.VariableRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for Variable management.
 */
public class VariableService {

    private final VariableRepository variableRepository;

    public VariableService(VariableRepository variableRepository) {
        this.variableRepository = variableRepository;
    }

    public Variable createVariable(Variable variable) {
        return variableRepository.save(variable);
    }

    public List<Variable> createVariablesBatch(List<Variable> variables) {
        return variableRepository.saveBatch(variables);
    }

    public List<Variable> getVariablesByInstance(UUID instanceId) {
        return variableRepository.findByEntityInstanceId(instanceId);
    }

    public Optional<Variable> getVariable(UUID instanceId, String variableName) {
        return variableRepository.findByInstanceIdAndName(instanceId, variableName);
    }

    public Map<String, Object> getVariablesAsMap(UUID instanceId) {
        List<Variable> variables = getVariablesByInstance(instanceId);
        Map<String, Object> result = new HashMap<>();

        for (Variable var : variables) {
            Object value = switch (var.variableType()) {
                case STRING -> var.asString();
                case INTEGER -> var.asLong();
                case FLOAT -> var.asDouble();
                case BOOLEAN -> var.asBoolean();
                case JSON -> var.asJson();
                case TIMESTAMP -> var.asTimestamp();
                case UUID -> var.asUuid();
                case BINARY -> var.asBinary();
            };
            result.put(var.variableName(), value);
        }

        return result;
    }
}
