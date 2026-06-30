package com.smartspend.backend.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

/**
 * Stores the AI-generated savings plan (a Map) as a JSON string in a
 * single TEXT/JSONB column, instead of normalizing it into more tables.
 * This is a deliberate trade-off: the plan is read/written as a whole
 * blob by the frontend, never queried field-by-field in SQL, so JSON
 * storage avoids unnecessary join complexity - good to mention if asked
 * "why not a separate PlanItem table" in a review.
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize plan to JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize plan JSON", e);
        }
    }
}
