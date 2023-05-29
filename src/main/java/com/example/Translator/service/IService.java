package com.example.Translator.service;

import com.example.Translator.dto.ListUnknownWordDTO;
import com.example.Translator.dto.TranslationCreationDTO;
import com.example.Translator.dto.TranslationDTO;

import java.util.List;

public interface IService {
    TranslationDTO translateWord(String word);
    TranslationDTO translateSentence(String sentence);
    List<ListUnknownWordDTO> listUnknownWords();
    String newTranslation(TranslationCreationDTO translationCreationDTO);

}
