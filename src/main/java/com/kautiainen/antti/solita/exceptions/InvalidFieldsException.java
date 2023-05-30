package com.kautiainen.antti.solita.exceptions;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An exception capable of indicating which field was invalid.
 */
public class InvalidFieldsException extends IllegalArgumentException {

    public static class FieldError implements Serializable {
        public final String fieldName;

        public final String description;

        public FieldError(String fieldName, String description) throws IllegalArgumentException {
            if (validFieldName(fieldName)) {
                this.fieldName = fieldName;
            } else {
                throw new IllegalArgumentException("Invalid field name");
            }
            if (validDescription(description)) {
                this.description = description;
            } else {
                throw new IllegalArgumentException("Invalid description");
            }
        }

        public FieldError(Map.Entry<String, String> entry) throws IllegalArgumentException {
            this(entry == null ? null : entry.getKey(), entry == null ? null : entry.getValue());
        }

        public boolean validDescription(String description) {
            return true;
        }

        public boolean validFieldName(String fieldName) {
            return true;
        }

        public Map.Entry<String, String> toMapEntry() {
            return new AbstractMap.SimpleEntry<String, String>(this.fieldName, this.description);
        }
    }

    private List<FieldError> errors = new ArrayList<FieldError>();

    public InvalidFieldsException(String message, Throwable cause, FieldError... errors) {
        super(message, cause);
        if (errors != null)
            this.errors.addAll(Arrays.asList(errors));
    }

    public List<String> getErrors(String fieldName) {
        return errors.stream().filter((FieldError error) -> (error.fieldName == fieldName)).map(
                (FieldError entry) -> (entry.description)).toList();
    }

    public boolean hasErrors(String fieldName) {
        return errors.stream().anyMatch((FieldError error) -> (error.fieldName == fieldName));
    }

    public List<String> getInvalidFields() {
        return errors.stream().collect(
                () -> (new ArrayList<String>()),
                (List<String> result, FieldError error) -> {
                    String sought = (error == null ? null : error.fieldName);
                    if (!result.contains(sought)) {
                        result.add(sought);
                    }
                },
                (List<String> first, List<String> second) -> {
                    first.addAll(second);
                });
    }
}