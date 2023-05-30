package com.example.Translator.service;

import com.example.Translator.dto.ListTranslationDTO;
import com.example.Translator.dto.ListUnknownWordDTO;
import com.example.Translator.dto.TranslationCreationDTO;
import com.example.Translator.dto.TranslationDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAppService {
    TranslationDTO translateWord(String word);
    TranslationDTO translateSentence(String sentence);
    List<ListUnknownWordDTO> listUnknownWords();
    String newTranslation(TranslationCreationDTO translationCreationDTO);
    List<ListTranslationDTO> listTranslations(Pageable pageable);

}
