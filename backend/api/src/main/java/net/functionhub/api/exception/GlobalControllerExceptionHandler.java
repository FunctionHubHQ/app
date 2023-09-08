package net.functionhub.api.exception;

import net.functionhub.api.service.utils.FHUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author Biz Melesse created on 8/9/23
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {

  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<FHErrorMessage> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        obfuscateMessage(ex),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<FHErrorMessage> internalError(Exception ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        obfuscateMessage(ex),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<FHErrorMessage> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        obfuscateMessage(ex),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
  }

  private String obfuscateMessage(Exception e) {
    if (e.getMessage().contains("org.springframework") ||
        e.getMessage().contains("java.") ||
        e.getMessage().contains("net.functionhub") ||
        e.getMessage().toLowerCase().contains("JDBC".toLowerCase())) {
      // TODO send all internal errors to slack
      e.printStackTrace();
      return "Internal Server Error";
    }
    return e.getMessage();
  }
}
