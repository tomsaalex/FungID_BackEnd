package com.example.fungid.exceptions.register;

public class UncompletedFieldsException extends RuntimeException{
    public UncompletedFieldsException(String message) {
        super(message);
    }
}
