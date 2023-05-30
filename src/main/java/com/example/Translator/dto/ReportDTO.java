package com.example.Translator.dto;

import java.util.Map;

public record ReportDTO(Long wordsNumber,
                        Map<String, Map<Integer, Long>> wordsLength,
                        Map<String, Double> averageLength,
                        Long unknownWordsNumber) {

}
