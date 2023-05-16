package com.example.Translator.service;

import com.example.Translator.report.Report;
import com.example.Translator.repository.ITranslationsRepository;
import com.example.Translator.repository.IUnknownWordsRepository;
import com.example.Translator.translation.Translation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ITranslationsRepository iTranslationsRepository;
    private final IUnknownWordsRepository iUnknownWordsRepository;

    public Report report() {
        Map<String, Map<Integer, Long>> wordsLengths = new HashMap<>();
        wordsLengths.put("POL", wordsInPolish());
        wordsLengths.put("ENG", wordsInEnglish());

        return new Report(
                iTranslationsRepository.count(),
                wordsLengths,
                averageLength(),
                iUnknownWordsRepository.count()
        );
    }

    private List<String> polishWordsList() {
        List<Translation> translations = iTranslationsRepository.findAll();
        List<String> polishWords = new ArrayList<>();
        for (Translation translation : translations) {
            polishWords.add(translation.getPolishWord());
        }

        polishWords.sort(Comparator.comparing(String::length));

        return polishWords;
    }

    private List<String> englishWordsList() {
        List<Translation> translations = iTranslationsRepository.findAll();
        List<String> englishWords = new ArrayList<>();
        for (Translation translation : translations) {
            englishWords.add(translation.getEnglishWord());
        }

        englishWords.sort(Comparator.comparing(String::length));

        return englishWords;
    }

    private Map<Integer, Long> wordsInPolish() {
        List<String> polishWords = polishWordsList();

        Map<Integer, Long> wordsLengthInPolish = new HashMap<>();
        for (String word : polishWords) {
            int length = word.length();
            if (wordsLengthInPolish.containsKey(length)) {
                wordsLengthInPolish.put(length, wordsLengthInPolish.get(length) + 1);
            } else {
                wordsLengthInPolish.put(length, 1L);
            }
        }
        log.info("Listed number of words of given length in polish");

        return wordsLengthInPolish;
    }

    private Map<Integer, Long> wordsInEnglish() {
        List<String> englishWords = englishWordsList();

        Map<Integer, Long> wordsLengthInEnglish = new HashMap<>();
        for (String word : englishWords) {
            int length = word.length();
            if (wordsLengthInEnglish.containsKey(length)) {
                wordsLengthInEnglish.put(length, wordsLengthInEnglish.get(length) + 1);
            } else {
                wordsLengthInEnglish.put(length, 1L);
            }
        }
        log.info("Listed number of words of given length in english");

        return wordsLengthInEnglish;
    }

    private Map<String, Double> averageLength() {
        List<String> polishWords = polishWordsList();
        List<String> englishWords = englishWordsList();

        long sumPol = 0L;
        for (String word : polishWords) {
            sumPol += word.length();
        }
        double averagePol = (double) sumPol / polishWords.size();
        log.info("Calculated average length of polish words: {}", averagePol);

        long sumEng = 0L;
        for (String word : englishWords) {
            sumEng += word.length();
        }
        double averageEng = (double) sumEng / englishWords.size();
        log.info("Calculated average length of english words: {}", averageEng);

        DecimalFormat df = new DecimalFormat("#.00");
        String roundedPol = df.format(averagePol);
        String roundedEng = df.format(averageEng);

        double roundedDoublePol = Double.parseDouble(roundedPol);
        double roundedDoubleEng = Double.parseDouble(roundedEng);

        Map<String, Double> averageLengthOfWord = new HashMap<>();
        averageLengthOfWord.put("POL", roundedDoublePol);
        averageLengthOfWord.put("ENG", roundedDoubleEng);

        return averageLengthOfWord;
    }

}
