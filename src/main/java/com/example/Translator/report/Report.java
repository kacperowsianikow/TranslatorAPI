package com.example.Translator.report;

import java.util.Map;

public record Report(Long wordsNumber,
                     Map<String, Map<Integer, Long>> wordsLength,
                     Map<String, Double> averageLength,
                     Long unknownWordsNumber) {

}
