package com.example.fungid.exceptions.register;

public class PasswordLengthExceededException extends RuntimeException{
    public PasswordLengthExceededException(String message) {
        super(message);
    }
}
