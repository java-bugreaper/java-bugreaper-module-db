package io.bugreaper.modules.db.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bugreaper.modules.db.exceptions.QueryBuilderException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonToArrayMapper {

    private JsonToArrayMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static Object[][] jsonToObjectArray(String json,  boolean addOptionsAfterValue) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (!root.isObject()) {
                throw new IllegalArgumentException("JSON must be a flat object");
            }


            List<Object[]> rows = new ArrayList<>();
            Iterator<String> fields = root.fieldNames();

            while (fields.hasNext()) {
                String rawKey = fields.next();
                JsonNode value = root.get(rawKey);

                //if more 2 elements parser
                String key = rawKey;
                String operator = null;

                if (rawKey.contains(":")) {
                    String[] parts = rawKey.split(":", 2);
                    key = parts[0];
                    operator = parts[1];
                }

                Object converted = convertValue(value);


                // adding order logic
                if (operator == null) {
                    rows.add(new Object[]{key, converted});
                } else {
                    if (addOptionsAfterValue) {
                        // key, value, operator
                        rows.add(new Object[]{key, converted, operator});
                    } else {
                        // key, operator, value
                        rows.add(new Object[]{key, operator, converted});
                    }
                }

            }

            return rows.toArray(new Object[0][0]);

        } catch (Exception e) {
            throw new QueryBuilderException("Failed to convert JSON to Object[][]: " + e.getMessage(), e);
        }
    }

    private static Object convertValue(JsonNode value) {
        if (value.isNull()) {
            return null;
        } else if (value.isBoolean()) {
            return value.booleanValue();
        } else if (value.isInt()) {
            return value.intValue();
        } else if (value.isFloat() || value.isDouble() || value.isBigDecimal()) {
            return value.asDouble();
        } else if (value.isObject() || value.isArray()) {
            return value.toString(); // JSON string
        } else {
            return value.asText();
        }
    }

}
