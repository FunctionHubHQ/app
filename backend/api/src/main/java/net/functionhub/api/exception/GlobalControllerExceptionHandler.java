package net.functionhub.api.exception;

import net.functionhub.api.service.utils.FHUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Biz Melesse created on 8/9/23
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<FHErrorMessage> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        obfuscateMessage(ex.getMessage()),
        HttpStatus.NOT_FOUND.value(),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<FHErrorMessage> internalError(Exception ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        obfuscateMessage(ex.getMessage()),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<FHErrorMessage> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        obfuscateMessage(ex.getMessage()),
        HttpStatus.BAD_REQUEST.value(),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
  }

  private String obfuscateMessage(String message) {
    if (message.contains("org.springframework") ||
        message.contains("java.") ||
        message.contains("net.functionhub")) {
      String[] tokens = message.split(":");
      return tokens[0];
    }
    if (message.toLowerCase().contains("JDBC exception".toLowerCase())) {
      return "Internal Server Error";
    }
    return message;
  }
}
