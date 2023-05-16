package com.example.Translator.service;

import com.example.Translator.repository.ITranslationsRepository;
import com.example.Translator.repository.IUnknownWordsRepository;
import com.example.Translator.translation.NewTranslationRequest;
import com.example.Translator.translation.SingleTranslationResponse;
import com.example.Translator.translation.Translation;
import com.example.Translator.translation.TranslationResponse;
import com.example.Translator.unknownwords.UnknownWord;
import com.example.Translator.unknownwords.UnknownWordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppService {
    private final ITranslationsRepository iTranslationsRepository;
    private final IUnknownWordsRepository iUnknownWordsRepository;

    public SingleTranslationResponse translateWord(String word) {
        Optional<Translation> byPolishWord =
                iTranslationsRepository.findByPolishWordIgnoreCase(word);
        if (byPolishWord.isPresent()) {
            log.info("Translation for POL word: {}, has been found", word);
            return new SingleTranslationResponse(byPolishWord.get().getEnglishWord());
        }

        Optional<Translation> byEnglishWord =
                iTranslationsRepository.findByEnglishWordIgnoreCase(word);
        if (byEnglishWord.isPresent()) {
            log.info("Translation for ENG word: {}, has been found", word);
            return new SingleTranslationResponse(byEnglishWord.get().getPolishWord());
        }

        log.info("No translation found for word: {}", word);
        return new SingleTranslationResponse(
                "Provided word doesn't exist in the database"
        );
    }

    public String translateSentence(String sentence) {
        if (sentence.isBlank()) {
            log.info("Provided blank input");
            return "Provided blank input";
        }
        String[] words = sentence.split("[,\\s]+");
        StringBuilder stringBuilder = new StringBuilder();

        for (String unclearedWord : words) {
            String word = unclearedWord.replaceAll("[.!?]+$", "");

            Optional<Translation> byPolishWord =
                    iTranslationsRepository.findByPolishWordIgnoreCase(word);
            if (byPolishWord.isPresent()) {
                log.info("Translation for POL word: {}, has been found", word);

                String englishTranslation = byPolishWord.get().getEnglishWord();
                if (stringBuilder.isEmpty()) {
                    char firstChar = Character.toUpperCase(englishTranslation.charAt(0));
                    stringBuilder
                            .append(firstChar)
                            .append(englishTranslation.substring(1))
                            .append(" ");
                    continue;
                }

                stringBuilder.append(englishTranslation).append(" ");
                continue;
            }

            Optional<Translation> byEnglishWord =
                    iTranslationsRepository.findByEnglishWordIgnoreCase(word);
            if (byEnglishWord.isPresent()) {
                log.info("Translation for ENG word: {}, has been found", word);

                String polishTranslation = byEnglishWord.get().getPolishWord();
                if (stringBuilder.isEmpty()) {
                    char firstLetter = Character.toUpperCase(polishTranslation.charAt(0));
                    stringBuilder
                            .append(firstLetter)
                            .append(polishTranslation.substring(1))
                            .append(" ");
                }
                stringBuilder.append(polishTranslation).append(" ");
                continue;
            }

            log.info("No translation found for word: {}", word);
            stringBuilder.append(word).append(" ");

            Optional<UnknownWord> byUnknownWord = iUnknownWordsRepository.findAllByUnknownWord(word);
            if (byUnknownWord.isEmpty()) {
                UnknownWord unknownWord = new UnknownWord(word);
                log.info("Unknown word: {} has been added to DB", word);
                iUnknownWordsRepository.save(unknownWord);
            }

        }

        return stringBuilder
                .deleteCharAt(stringBuilder.length() - 1)
                .append(".")
                .toString().trim();
    }

    public List<UnknownWordResponse> listUnknownWords() {
        List<UnknownWord> allUnknownWords = iUnknownWordsRepository.findAll();
        log.info("Downloaded unknown words from DB");
        List<UnknownWordResponse> allUnknownWordsOutput = new LinkedList<>();

        for (UnknownWord word : allUnknownWords) {
            UnknownWordResponse unknownWordResponse = new UnknownWordResponse(
                    word.getId(),
                    word.getUnknownWord()
            );
            allUnknownWordsOutput.add(unknownWordResponse);
            log.info("Added all unknown words to the list");
        }

        return allUnknownWordsOutput;
    }

    public String newTranslation(NewTranslationRequest newTranslationRequest) {
        Optional<Translation> byEnglishWord =
                iTranslationsRepository.findByEnglishWordIgnoreCase(newTranslationRequest.englishWord());
        if (byEnglishWord.isPresent()) {
            log.info("Provided translation already exists");
            return "Provided translation already exists";
        }

        Translation translation = new Translation(
                newTranslationRequest.polishWord(),
                newTranslationRequest.englishWord()
        );
        iTranslationsRepository.save(translation);

        log.info("New translation added to the dictionary");
        return "New translation added to the dictionary";
    }

    public List<TranslationResponse> listTranslations(Pageable pageable) {
        Page<Translation> page = iTranslationsRepository.findAll(pageable);
        log.info("Found {} translations", page.getTotalElements());

        List<Translation> translationList = page.getContent();
        log.info("Converting page to list");
        List<TranslationResponse> listResponse = new LinkedList<>();
        for (Translation translation : translationList) {
            TranslationResponse response = new TranslationResponse(
                    translation.getId(),
                    translation.getPolishWord(),
                    translation.getEnglishWord()
            );
            listResponse.add(response);
        }

        return listResponse;
    }

}
