package net.functionhub.api.exception;

import net.functionhub.api.service.utils.FHUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * @author Biz Melesse created on 8/9/23
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<FHErrorMessage> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        ex.getMessage(),
        HttpStatus.NOT_FOUND.value(),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<FHErrorMessage> resourceNotFoundException(Exception ex, WebRequest request) {
    FHErrorMessage message = new FHErrorMessage(
        ex.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        FHUtils.getCurrentTime());

    return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
