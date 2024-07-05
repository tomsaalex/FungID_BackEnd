package com.example.fungid.exceptions.register;

public class EmailLengthExceededException extends RuntimeException{
    public EmailLengthExceededException(String message) {
        super(message);
    }
}
