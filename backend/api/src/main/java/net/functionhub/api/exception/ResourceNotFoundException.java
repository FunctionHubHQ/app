package net.functionhub.api.exception;

/**
 * @author Biz Melesse created on 8/9/23
 */
public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ResourceNotFoundException(String msg) {
    super(msg);
  }
}