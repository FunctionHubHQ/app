package net.functionhub.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Biz Melesse created on 12/24/22
 */
@Getter
@Setter
public class Pair <S, T>{
  private S first;
  private T second;

  public Pair(S first, T second) {
    this.first = first;
    this.second = second;
  }
}
