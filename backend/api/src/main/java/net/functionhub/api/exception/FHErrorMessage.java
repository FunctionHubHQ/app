package net.functionhub.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Biz Melesse created on 8/9/23
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class FHErrorMessage {
  private String error;
  private int code;
  private String timestamp;
}


