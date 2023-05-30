package com.example.Translator.mapper;

import com.example.Translator.dto.*;
import com.example.Translator.report.Report;
import com.example.Translator.translation.Translation;
import com.example.Translator.unknownword.UnknownWord;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    public TranslationDTO toTranslationDTO(String output) {
        return new TranslationDTO(output);
    }

    public ListTranslationDTO toListTranslationDTO(Translation translation) {
        return new ListTranslationDTO(
                translation.getId(),
                translation.getPolishWord(),
                translation.getEnglishWord()
        );
    }

    public ListUnknownWordDTO toListUnknownWordDTO(UnknownWord unknownWord) {
        return new ListUnknownWordDTO(
                unknownWord.getId(),
                unknownWord.getUnknownWord()
        );
    }

    public ReportDTO toReportDTO(Report report) {
        return new ReportDTO(
                report.wordsNumber(),
                report.wordsLength(),
                report.averageLength(),
                report.unknownWordsNumber()
        );
    }

    public UnknownWord toUnknownWord(TranslationDTO translationDTO) {
        return new UnknownWord(translationDTO.translation());
    }

    public Translation toTranslation(TranslationCreationDTO translationCreationDTO) {
        return new Translation(
                translationCreationDTO.polishWord(),
                translationCreationDTO.englishWord()
        );
    }

}
