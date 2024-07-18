package com.example.fungid.controller;

import com.example.fungid.exceptions.login.InvalidCredentialsException;
import com.example.fungid.exceptions.mushroom_id.*;
import com.example.fungid.exceptions.register.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(value
            = {UncompletedFieldsException.class})
    protected ResponseEntity<String> handleRegisterUncompletedFieldsException(
            UncompletedFieldsException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UsernameLengthExceededException.class})
    protected ResponseEntity<String> handleUsernameLengthExceededException(
            UsernameLengthExceededException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {PasswordLengthExceededException.class})
    protected ResponseEntity<String> handlePasswordLengthExceededException(
            PasswordLengthExceededException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EmailLengthExceededException.class})
    protected ResponseEntity<String> handleEmailLengthExceededException(
            EmailLengthExceededException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UsernameTakenException.class})
    protected ResponseEntity<String> handleUsernameTakenException(
            UsernameTakenException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {EmailTakenException.class})
    protected ResponseEntity<String> handleEmailTakenException(
            EmailTakenException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {InvalidCredentialsException.class})
    protected ResponseEntity<String> handleInvalidCredentialsException(
            InvalidCredentialsException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    protected ResponseEntity<String> handleHttpMessageNotReadableException() {
        String bodyOfResponse = "There is a problem with the request body. Please check the request body and try again.";
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ResourceAccessException.class})
    protected ResponseEntity<String> handleResourceAccessException(
            ResourceAccessException ex) {
        if(ex.getMessage().contains("Connection refused"))
            return new ResponseEntity<>("The server is currently unavailable for classification jobs. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        String bodyOfResponse = "There was a problem accessing the desired resource. Please try again later.";
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = {DateTimeParseException.class})
    protected ResponseEntity<String> handleDateTimeParseException() {
        String bodyOfResponse = "The date provided is not in the correct format. Please provide a date in the format yyyy-MM-dd-HH-mm-ss-SSS.";
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MushroomImageProcessingException.class})
    protected ResponseEntity<String> handleMushroomImageProcessingException() {
        String bodyOfResponse = "There was a problem processing the image of the mushroom. Please try again later.";
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {MushroomImageMissingException.class})
    protected ResponseEntity<String> handleMushroomImageMissingException(
            MushroomImageMissingException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ImageTypeNotSupportedException.class})
    protected ResponseEntity<String> handleImageTypeNotSupportedException(
            ImageTypeNotSupportedException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MissingServletRequestPartException.class})
    protected ResponseEntity<String> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        String bodyOfResponse = "The multipart request is missing one or more parts." + ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MushroomImageRetrievalException.class})
    protected ResponseEntity<String> handleMushroomImageRetrievalException() {
        String bodyOfResponse = "There was a problem retrieving the image of the mushroom. Please try again later.";
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {MushroomNotFoundException.class})
    protected ResponseEntity<String> handleMushroomNotFoundException(
            MushroomNotFoundException ex) {
        String bodyOfResponse = ex.getMessage();
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid parameter value: '%s' for parameter: '%s'. Expected type: %s.",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName(): "unknown");
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException() {
        String message = "The requested resource was not found.";
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<String> handleException() {
        String bodyOfResponse = "An unknown exception occurred";
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
