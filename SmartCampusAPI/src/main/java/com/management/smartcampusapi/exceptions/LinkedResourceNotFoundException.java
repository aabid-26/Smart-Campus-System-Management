package com.management.smartcampusapi.exceptions;

public class LinkedResourceNotFoundException extends RuntimeException {

    private final String fieldName;
    private final String fieldValue;

    public LinkedResourceNotFoundException(String fieldName, String fieldValue) {
        super("Referenced " + fieldName + " '" + fieldValue + "' does not exist in the system.");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() { return fieldName; }
    public String getFieldValue() { return fieldValue; }
}
