package net.functionhub.api.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 8/7/23
 */
@Component
public class WordList {
  private final List<String> wordList = new ArrayList<>();

  public WordList() {
    String words = FHUtils.loadFile("wordList.txt");
    if (!ObjectUtils.isEmpty(words)) {
      wordList.addAll(Stream.of(words.split(","))
          .map(it -> it.replace("\n", "").strip())
          .filter(it -> !ObjectUtils.isEmpty(it))
          .toList());
    }
  }

  public String getRandomPhrase(int wordLength) {
    StringJoiner joiner = new StringJoiner("-");
    Random rand = new Random();
    for (int i = 0; i < wordLength; i++) {
      joiner.add(wordList.get(rand.nextInt(wordList.size())).toLowerCase());
    }
    return joiner.toString();
  }
}
