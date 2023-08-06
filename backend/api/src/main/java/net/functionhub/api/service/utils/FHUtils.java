package net.functionhub.api.service.utils;

import java.util.Random;

/**
 * @author Biz Melesse created on 1/2/23
 */
public class FHUtils {
  public static final int SHORT_UID_LENGTH = 8;
  public static final int LONG_UID_LENGTH = 16;

  public static String generateUid(int length) {
    StringBuilder builder = new StringBuilder();
    // An ID length of N gives 62^N unique IDs
    for (int i = 0; i < length; i++) {
      builder.append(getRandomCharacter());
    }
    return builder.toString();
  }

  public static Character getRandomCharacter() {
    Random random = new Random();
    String uidAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnoqprstuvwxyz0123456789";
    int index = random.nextInt(uidAlphabet.length());
    return uidAlphabet.charAt(index);
  }

  public static String truncate(String value) {
    int maxLength = 255;
    if (value.length() > maxLength) {
      return value.substring(0, maxLength);
    }
    return value;
  }
}
