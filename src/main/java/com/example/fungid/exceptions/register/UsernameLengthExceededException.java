package com.example.fungid.exceptions.register;

public class UsernameLengthExceededException extends RuntimeException{
    public UsernameLengthExceededException(String message) {
        super(message);
    }
}
