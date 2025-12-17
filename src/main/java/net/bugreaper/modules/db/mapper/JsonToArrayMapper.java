package net.bugreaper.modules.db.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonToArrayMapper {

    private JsonToArrayMapper() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Converts a flat JSON object into a two-dimensional {@code Object[][]} array.
     * <p>
     * Supports JSON keys with up to two options separated by ':'.
     * The resulting row may contain 2–4 elements.
     * <p>
     * Each JSON entry is converted into one row of the resulting array:
     * <ul>
     *   <li>{@code {"key": value}} → {@code {"key", convertedValue}}</li>
     *   <li>{@code {"key:OPTION": value}} →
     *     <ul>
     *       <li>if {@code addOptionsAfterValue == true} →
     *           {@code {"key", convertedValue, "OPTION"}}</li>
     *       <li>if {@code addOptionsAfterValue == false} →
     *           {@code {"key", "OPTION", convertedValue}}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p>Value conversion rules:
     * <ul>
     *   <li>JSON number (int/long) → {@link Integer}/{@link Long}</li>
     *   <li>JSON decimal → {@link String}</li>
     *   <li>JSON string → {@link String}</li>
     *   <li>JSON boolean → {@link Boolean}</li>
     *   <li>JSON {@code null} → {@code null}</li>
     *   <li>JSON object or array → compact JSON {@link String}</li>
     * </ul>
     *
     * @param json the JSON string representing a flat object
     * @param addOptionsAfterValue controls the order of elements when a key contains options
     *                              separated by {@code ':'}.
     *                              If {@code true}, the order is {@code key, value, option}.
     *                              If {@code false}, the order is {@code key, option, value}.
     * @return a two-dimensional {@code Object[][]} representation of the JSON object
     * @throws QueryBuilderException if the provided JSON is not a flat JSON object or parsing/conversion fails
     */
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
                JsonNode valueNode = root.get(rawKey);

                String[] parts = rawKey.split(":");
                if (parts.length > 3) {
                    throw new QueryBuilderException(
                            "Maximum supported key format is FIELD:OPT1:OPT2, but got: " + rawKey
                    );
                }

                String fieldName = parts[0];
                Object value = convertValue(valueNode);

                // No options
                if (parts.length == 1) {
                    rows.add(new Object[]{fieldName, value});
                    continue;
                }

                // Collect options
                String[] options = new String[parts.length - 1];
                System.arraycopy(parts, 1, options, 0, options.length);

                if (addOptionsAfterValue) {
                    // FIELD, VALUE, OPT1, OPT2
                    Object[] row = new Object[2 + options.length];
                    row[0] = fieldName;
                    row[1] = value;
                    System.arraycopy(options, 0, row, 2, options.length);
                    rows.add(row);
                } else {
                    // FIELD, OPT1, OPT2, VALUE
                    Object[] row = new Object[2 + options.length];
                    row[0] = fieldName;
                    System.arraycopy(options, 0, row, 1, options.length);
                    row[row.length - 1] = value;
                    rows.add(row);
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
