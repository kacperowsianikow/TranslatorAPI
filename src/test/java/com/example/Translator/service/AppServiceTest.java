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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AppServiceTest {
    @Mock
    private ITranslationsRepository iTranslationsRepository;
    @Mock
    private IUnknownWordsRepository iUnknownWordsRepository;
    @Mock
    private Mapper mapper;
    private AppService appService;

    @BeforeEach
    void setUp() {
        appService = new AppService(
                iTranslationsRepository,
                iUnknownWordsRepository,
                mapper
        );
    }

    @Test
    void translateWord_successfullyTranslatedToEnglish() {
        String input = "żołnierz";
        Translation translation = new Translation(
                "żołnierz",
                "soldier"
        );

        when(iTranslationsRepository.findByPolishWordIgnoreCase(input))
                .thenReturn(Optional.of(translation));
        when(mapper.toTranslationDTO(translation.getEnglishWord()))
                .thenReturn(new TranslationDTO("soldier"));

        TranslationDTO result = appService.translateWord(input);

        assertThat(result.translation()).isEqualTo("soldier");

        verify(iTranslationsRepository).findByPolishWordIgnoreCase(input);

        verify(mapper).toTranslationDTO(translation.getEnglishWord());
    }

    @Test
    void translateWord_successfullyTranslatedToPolish() {
        String input = "oak";
        Translation translation = new Translation(
                "dąb",
                "oak"
        );

        when(iTranslationsRepository.findByEnglishWordIgnoreCase(input))
                .thenReturn(Optional.of(translation));
        when(mapper.toTranslationDTO(translation.getPolishWord()))
                .thenReturn(new TranslationDTO("dąb"));

        TranslationDTO result = appService.translateWord(input);

        assertThat(result.translation()).isEqualTo("dąb");

        verify(iTranslationsRepository).findByEnglishWordIgnoreCase(input);

        verify(mapper).toTranslationDTO(translation.getPolishWord());
    }

    @Test
    void translateWord_wordNotExistingInDBTranslation() {
        String input = "ice cream";

        when(iTranslationsRepository.findByPolishWordIgnoreCase(input))
                .thenReturn(Optional.empty());
        when(iTranslationsRepository.findByEnglishWordIgnoreCase(input))
                .thenReturn(Optional.empty());
        when(mapper.toTranslationDTO("Provided word doesn't exist in the database"))
                .thenReturn(new TranslationDTO("Provided word doesn't exist in the database"));

        TranslationDTO result = appService.translateWord(input);

        assertThat(result.translation()).isEqualTo("Provided word doesn't exist in the database");

        verify(iTranslationsRepository).findByPolishWordIgnoreCase(input);
        verify(iTranslationsRepository).findByEnglishWordIgnoreCase(input);

        verify(mapper).toTranslationDTO("Provided word doesn't exist in the database");
    }

    @Test
    void translateSentence_successfullyTranslatedAllWords() {
        String input = "Kot, goni mysz.";
        TranslationDTO expected = new TranslationDTO("Cat chases mouse.");

        when(iTranslationsRepository.findByPolishWordIgnoreCase("Kot"))
                .thenReturn(Optional.of(new Translation("kot", "cat")));
        when(iTranslationsRepository.findByPolishWordIgnoreCase("goni"))
                .thenReturn(Optional.of(new Translation("goni", "chases")));
        when(iTranslationsRepository.findByPolishWordIgnoreCase("mysz"))
                .thenReturn(Optional.of(new Translation("mysz", "mouse")));
        when(mapper.toTranslationDTO("Cat chases mouse."))
                .thenReturn(expected);

        TranslationDTO result = appService.translateSentence(input);

        assertThat(result.translation()).isEqualTo(expected.translation());

        verify(iTranslationsRepository).findByPolishWordIgnoreCase("Kot");
        verify(iTranslationsRepository).findByPolishWordIgnoreCase("goni");
        verify(iTranslationsRepository).findByPolishWordIgnoreCase("mysz");

        verify(mapper).toTranslationDTO(expected.translation());
    }

    @Test
    void translateSentence_onlyOneWordNotTranslated() {
        String input = "stół bez nóg";
        TranslationDTO expected = new TranslationDTO("Table bez legs.");

        when(iTranslationsRepository.findByPolishWordIgnoreCase("stół"))
                .thenReturn(Optional.of(new Translation("stół", "table")));
        when(iTranslationsRepository.findByPolishWordIgnoreCase("bez"))
                .thenReturn(Optional.empty());
        when(iTranslationsRepository.findByPolishWordIgnoreCase("nóg"))
                .thenReturn(Optional.of(new Translation("nóg", "legs")));
        when(mapper.toTranslationDTO("Table bez legs."))
                .thenReturn(expected);

        TranslationDTO result = appService.translateSentence(input);

        assertThat(result.translation()).isEqualTo(expected.translation());

        verify(iTranslationsRepository).findByPolishWordIgnoreCase("stół");
        verify(iTranslationsRepository).findByPolishWordIgnoreCase("bez");
        verify(iTranslationsRepository).findByPolishWordIgnoreCase("nóg");

        verify(mapper).toTranslationDTO(expected.translation());
    }

    @Test
    void translateSentence_providedBlankInputShouldReturnInfo() {
        String input = " ";

        TranslationDTO expected = new TranslationDTO("Provided blank input");

        when(mapper.toTranslationDTO("Provided blank input"))
                .thenReturn(expected);

        TranslationDTO result = appService.translateSentence(input);

        assertThat(result).isEqualTo(expected);

        verify(mapper).toTranslationDTO("Provided blank input");
    }

    @Test
    void newTranslation_successfullyAddedToTheDB() {
        TranslationCreationDTO translatioCreationDTO = new TranslationCreationDTO(
                "żółty",
                "yellow"
        );
        Translation translation = new Translation(
                "żółty",
                "yellow"
        );

        when(iTranslationsRepository.findByEnglishWordIgnoreCase("yellow"))
                .thenReturn(Optional.empty());
        when(mapper.toTranslation(translatioCreationDTO))
                .thenReturn(translation);

        String result = appService.newTranslation(translatioCreationDTO);

        assertThat(result).isEqualTo("New translation added to the dictionary");

        verify(iTranslationsRepository).save(translation);

        verify(mapper).toTranslation(translatioCreationDTO);
    }

    @Test
    void newTranslation_providedTranslationAlreadyExists() {
        Translation translation = new Translation(
                "kot",
                "cat"
        );
        TranslationCreationDTO translationCreationDTO = new TranslationCreationDTO(
                "kot",
                "cat"
        );

        when(iTranslationsRepository.findByEnglishWordIgnoreCase(translationCreationDTO.englishWord()))
                .thenReturn(Optional.of(translation));

        String result = appService.newTranslation(translationCreationDTO);

        assertThat(result).isEqualTo("Provided translation already exists");

        verify(iTranslationsRepository, never()).save(any());
    }

    @Test
    void listTranslations_shouldReturnSuccessfully() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id"));

        List<Translation> translations = Arrays.asList(
                new Translation("kot", "cat"),
                new Translation("pies", "dog")
        );

        List<ListTranslationDTO> expected = Arrays.asList(
                new ListTranslationDTO(1L, "kot", "cat"),
                new ListTranslationDTO(2L, "pies", "dog")
        );

        Page<Translation> page = new PageImpl<>(translations, pageable, translations.size());

        when(iTranslationsRepository.findAll(pageable))
                .thenReturn(page);
        when(mapper.toListTranslationDTO(translations.get(0)))
                .thenReturn(expected.get(0));
        when(mapper.toListTranslationDTO(translations.get(1)))
                .thenReturn(expected.get(1));

        List<ListTranslationDTO> translationsDTOS = appService.listTranslations(pageable);

        assertThat(translationsDTOS).hasSize(2);
        assertThat(translationsDTOS).isEqualTo(expected);

        verify(iTranslationsRepository).findAll(pageable);

        verify(mapper).toListTranslationDTO(translations.get(0));
        verify(mapper).toListTranslationDTO(translations.get(1));
    }

    @Test
    void listUnknownWords_shouldReturnSuccessfully() {
        List<UnknownWord> unknownWords = Arrays.asList(
                new UnknownWord("dom"),
                new UnknownWord("kamienica")
        );

        List<ListUnknownWordDTO> expected = Arrays.asList(
                new ListUnknownWordDTO(1L, "dom"),
                new ListUnknownWordDTO(2L, "kamienica")
        );

        when(iUnknownWordsRepository.findAll())
                .thenReturn(unknownWords);
        when(mapper.toListUnknownWordDTO(unknownWords.get(0)))
                .thenReturn(expected.get(0));
        when(mapper.toListUnknownWordDTO(unknownWords.get(1)))
                .thenReturn(expected.get(1));

        List<ListUnknownWordDTO> unknownWordDTOS = appService.listUnknownWords();

        assertThat(unknownWordDTOS).hasSize(2);
        assertThat(unknownWordDTOS).isEqualTo(expected);

        verify(iUnknownWordsRepository).findAll();

        verify(mapper).toListUnknownWordDTO(unknownWords.get(0));
        verify(mapper).toListUnknownWordDTO(unknownWords.get(1));
    }

}
