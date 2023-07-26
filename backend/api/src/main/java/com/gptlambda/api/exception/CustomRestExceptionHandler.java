package com.gptlambda.api.exception;

import com.gptlambda.api.GPTLambdaExceptionResponse;
import com.gptlambda.api.GenericResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Biz Melesse created on 7/26/23
 */
@ControllerAdvice
@Slf4j
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

//    /**
//     * BindException: This exception is thrown when fatal binding errors occur.
//     * MethodArgumentNotValidException:
//     * This exception is thrown when argument annotated with @Valid failed validation:
//     */
//    @Nonnull
//    @Override
//    public ResponseEntity<Object> handleMethodArgumentNotValid(
//            @Nonnull MethodArgumentNotValidException ex,
//            @Nonnull HttpHeaders headers,
//            @Nonnull HttpStatus status,
//            @Nonnull WebRequest request
//    ) {
//        List<String> errors = new ArrayList<>();
//        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
//            errors.add(error.getField() + ": " + error.getDefaultMessage());
//        }
//        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
//            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
//        }
//
//        String err = errors.stream()
//                .collect(Collectors.joining(";", "{", "}"));
//
//        GenericResponse body = new GenericResponse();
//        body.setStatus(HttpStatus.BAD_REQUEST.toString());
//        body.setError(err);
//
//        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
//    }
//
//    /**
//     * MissingServletRequestPartException: This exception is thrown when the part of a multipart request not found
//     * MissingServletRequestParameterException: This exception is thrown when request missing parameter:
//     */
//    @Nonnull
//    @Override
//    public ResponseEntity<Object> handleMissingServletRequestParameter(
//            @Nonnull MissingServletRequestParameterException ex, @Nonnull HttpHeaders headers,
//            @Nonnull HttpStatus status, @Nonnull WebRequest request
//    ) {
//        String error = ex.getParameterName() + " parameter is missing";
//
//        GenericResponse body = new GenericResponse();
//        body.setStatus(HttpStatus.BAD_REQUEST.toString());
//        body.setError(error);
//        return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
//    }

    /*
     * ConstrainViolationException: This exception reports the result of constraint violations:
     */

    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<Object> handleAccessDeniedException(
        Exception ex, WebRequest request) {
        return new ResponseEntity<Object>(
            "Access denied message here", new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request
    ) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " +
                    violation.getPropertyPath() + ": " + violation.getMessage());
        }

        String err = errors.stream()
                .collect(Collectors.joining(";", "{", "}"));
        GenericResponse body = new GenericResponse();
        body.setStatus((HttpStatus.BAD_REQUEST.toString()));
        body.setError((err));

        return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * TypeMismatchException: This exception is thrown when try to set bean property with wrong type.
     * MethodArgumentTypeMismatchException: This exception is thrown when method argument is not the expected type:
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request
    ) {
        String error = ex.getName() + " should be of type " + Objects.requireNonNull(ex.getRequiredType()).getName();

        GenericResponse body = new GenericResponse();
        body.setStatus((HttpStatus.BAD_REQUEST.toString()));
        body.setError((error));

        return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {
            GPTLambdaControllerException.class
            , GPTLambdaValidationException.class
    })
    public ResponseEntity<Object> handleKippoSpecificException(
            RuntimeException ex, WebRequest request) {
        GenericResponse body = new GenericResponse();
        String bodyOfResponse = ex.getMessage(); // This should be application specific
        body.setStatus((HttpStatus.INTERNAL_SERVER_ERROR.toString()));
        body.setError((bodyOfResponse));
        return handleExceptionInternal(ex, body,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = GPTLambdaException.class)
    public ResponseEntity<Object> handleKippoException(GPTLambdaException ex, WebRequest request) {
        GPTLambdaExceptionResponse response = new GPTLambdaExceptionResponse();
        response.setStatus((HttpStatus.INTERNAL_SERVER_ERROR.toString()));
        if (ex.getTitle() != null) {
            response.setTitle((ex.getTitle()));
        }
        if (ex.getErrMessage() != null) {
            response.setMessage((ex.getErrMessage()));
        }
        return handleExceptionInternal(ex, response,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
