package com.example.Translator.service;

import com.example.Translator.dto.ListTranslationDTO;
import com.example.Translator.dto.ListUnknownWordDTO;
import com.example.Translator.dto.TranslationCreationDTO;
import com.example.Translator.dto.TranslationDTO;
import com.example.Translator.mapper.Mapper;
import com.example.Translator.repository.ITranslationsRepository;
import com.example.Translator.repository.IUnknownWordsRepository;
import com.example.Translator.translation.Translation;
import com.example.Translator.unknownword.UnknownWord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppService implements IService {
    private final ITranslationsRepository iTranslationsRepository;
    private final IUnknownWordsRepository iUnknownWordsRepository;
    private final Mapper mapper;

    public TranslationDTO translateWord(String word) {
        Optional<Translation> byPolishWord =
                iTranslationsRepository.findByPolishWordIgnoreCase(word);
        if (byPolishWord.isPresent()) {
            log.info("Translation for POL word: {}, has been found", word);
            return mapper.toTranslationDTO(byPolishWord.get().getEnglishWord());
        }

        Optional<Translation> byEnglishWord =
                iTranslationsRepository.findByEnglishWordIgnoreCase(word);
        if (byEnglishWord.isPresent()) {
            log.info("Translation for ENG word: {}, has been found", word);
            return mapper.toTranslationDTO(byEnglishWord.get().getPolishWord());
        }

        Optional<UnknownWord> byUnknownWord =
                iUnknownWordsRepository.findAllByUnknownWord(word);
        if (byUnknownWord.isEmpty()) {
            TranslationDTO unknownWord = new TranslationDTO(word);
            log.info("Unknown word: {} has been added to DB", word);
            iUnknownWordsRepository.save(mapper.toUnknownWord(unknownWord));
        }

        log.info("No translation found for word: {}", word);

        return mapper.toTranslationDTO("Provided word doesn't exist in the database");
    }

    public TranslationDTO translateSentence(String sentence) {
        if (sentence.isBlank()) {
            log.info("Provided blank input");
            return mapper.toTranslationDTO("Provided blank input");
        }

        String[] words = sentence.split("[,\\s]+");
        StringBuilder stringBuilder = new StringBuilder();

        for (String unclearedWord : words) {
            String word = unclearedWord.replaceAll("[.!?]+$", "");

            Optional<Translation> translation = findTranslation(word);
            if (translation.isPresent()) {
                log.info("Translation for POL word: {}, has been found", word);

                String translatedWord = getTranslatedWord(translation.get(), word);
                stringBuilder.append(translatedWord).append(" ");
                continue;
            }

            log.info("No translation found for word: {}", word);
            stringBuilder.append(word).append(" ");

            Optional<UnknownWord> byUnknownWord =
                    iUnknownWordsRepository.findAllByUnknownWord(word);
            if (byUnknownWord.isEmpty()) {
                TranslationDTO unknownWord = new TranslationDTO(word);
                log.info("Unknown word: {} has been added to DB", word);
                iUnknownWordsRepository.save(mapper.toUnknownWord(unknownWord));
            }

        }

        String result = stringBuilder.deleteCharAt(stringBuilder.length() - 1)
                .append(".")
                .toString();

        return mapper.toTranslationDTO(
                Character.toUpperCase(result.charAt(0)) +
                result.substring(1)
        );
    }

    private Optional<Translation> findTranslation(String word) {
        Optional<Translation> byPolishWord =
                iTranslationsRepository.findByPolishWordIgnoreCase(word);
        if (byPolishWord.isPresent()) {
            return byPolishWord;
        }

        return iTranslationsRepository.findByEnglishWordIgnoreCase(word);
    }

    private String getTranslatedWord(Translation translation, String word) {
        if (translation.getPolishWord().equalsIgnoreCase(word)) {
            return translation.getEnglishWord();
        }

        return translation.getPolishWord();
    }

    public List<ListUnknownWordDTO> listUnknownWords() {
        List<UnknownWord> unknownWords = iUnknownWordsRepository.findAll();
        log.info("Downloaded unknown words from DB");

        return unknownWords.stream()
                .map(mapper::toListUnknownWordDTO)
                .collect(Collectors.toList());
    }

    public List<ListTranslationDTO> listTranslations(Pageable pageable) {
        Page<Translation> page = iTranslationsRepository.findAll(pageable);
        log.info("Found {} translations", page.getTotalElements());

        List<Translation> translations = page.getContent();
        log.info("Converting page to list");

        return translations.stream()
                .map(mapper::toListTranslationDTO)
                .collect(Collectors.toList());
    }

    public String newTranslation(TranslationCreationDTO translationCreationDTO) {
        Optional<Translation> byEnglishWord =
                iTranslationsRepository.findByEnglishWordIgnoreCase(translationCreationDTO.englishWord());
        if (byEnglishWord.isPresent()) {
            log.info("Provided translation already exists");
            return "Provided translation already exists";
        }

        iTranslationsRepository.save(mapper.toTranslation(translationCreationDTO));

        log.info("New translation added to the dictionary");
        return "New translation added to the dictionary";
    }

}
