package com.example.Translator.service;

import com.example.Translator.repository.ITranslationsRepository;
import com.example.Translator.repository.IUnknownWordsRepository;
import com.example.Translator.translation.NewTranslationRequest;
import com.example.Translator.translation.SingleTranslationResponse;
import com.example.Translator.translation.Translation;
import com.example.Translator.translation.TranslationResponse;
import com.example.Translator.unknownwords.UnknownWord;
import com.example.Translator.unknownwords.UnknownWordResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AppServiceTest {
    @Mock
    private ITranslationsRepository iTranslationsRepository;
    @Mock
    private IUnknownWordsRepository iUnknownWordsRepository;
    private AppService appService;

    @BeforeEach
    void setUp() {
        iTranslationsRepository = mock(ITranslationsRepository.class);
        iUnknownWordsRepository = mock(IUnknownWordsRepository.class);
        appService = new AppService(iTranslationsRepository, iUnknownWordsRepository);
    }

    @Test
    void translateWord_translatedToEnglish() {
        String input = "żołnierz";
        Translation translation = new Translation(
                "żołnierz",
                "soldier"
        );

        when(iTranslationsRepository.findByPolishWordIgnoreCase(input))
                .thenReturn(Optional.of(translation));

        SingleTranslationResponse result = appService.translateWord(input);

        assertThat(result.translatedWord()).isEqualTo("soldier");
    }

    @Test
    void translateWord_translatedToPolish() {
        String input = "oak";
        Translation translation = new Translation(
                "dąb",
                "oak"
        );

        when(iTranslationsRepository.findByEnglishWordIgnoreCase(input))
                .thenReturn(Optional.of(translation));

        SingleTranslationResponse result = appService.translateWord(input);

        assertThat(result.translatedWord()).isEqualTo("dąb");
    }

    @Test
    void translateWord_nonExistingTranslation() {
        String input = "ice cream";

        when(iTranslationsRepository.findByPolishWordIgnoreCase(input))
                .thenReturn(Optional.empty());
        when(iTranslationsRepository.findByEnglishWordIgnoreCase(input))
                .thenReturn(Optional.empty());

        SingleTranslationResponse result = appService.translateWord(input);

        assertThat(result.translatedWord()).isEqualTo("Provided word doesn't exist in the database");
    }

    @Test
    void translateSentence_translatedAllWords() {
        String input = "Kot, goni mysz.";

        when(iTranslationsRepository.findByPolishWordIgnoreCase("Kot"))
                .thenReturn(Optional.of(new Translation("kot", "cat")));
        when(iTranslationsRepository.findByPolishWordIgnoreCase("goni"))
                .thenReturn(Optional.of(new Translation("goni", "chases")));
        when(iTranslationsRepository.findByPolishWordIgnoreCase("mysz"))
                .thenReturn(Optional.of(new Translation("mysz", "mouse")));

        String result = appService.translateSentence(input);

        assertThat(result).isEqualTo("Cat chases mouse.");
    }

    @Test
    void translateSentence_oneWordNotTranslated() {
        String input = "stół bez nóg";

        when(iTranslationsRepository.findByPolishWordIgnoreCase("stół"))
                .thenReturn(Optional.of(new Translation("stół", "table")));
        when(iTranslationsRepository.findByPolishWordIgnoreCase("bez"))
                .thenReturn(Optional.empty());
        when(iTranslationsRepository.findByPolishWordIgnoreCase("nóg"))
                .thenReturn(Optional.of(new Translation("nóg", "legs")));

        String result = appService.translateSentence(input);

        assertThat(result).isEqualTo("Table bez legs.");
    }

    @Test
    void translateSentence_blankInput() {
        String input = " ";

        String result = appService.translateSentence(input);

        assertThat(result).isEqualTo("Provided blank input");
    }

    @Test
    void listUnknownWords() {
        UnknownWord firstUnknown = new UnknownWord(1L, "dom");
        UnknownWord secondUnknown = new UnknownWord(2L, "kamienica");
        List<UnknownWord> allUnknownWords = Arrays.asList(firstUnknown, secondUnknown);

        when(iUnknownWordsRepository.findAll()).thenReturn(allUnknownWords);

        List<UnknownWordResponse> allUnknownWordsOutput = appService.listUnknownWords();
        UnknownWordResponse firstResult = allUnknownWordsOutput.get(0);
        UnknownWordResponse secondResult = allUnknownWordsOutput.get(1);

        assertThat(firstResult.id()).isEqualTo(firstUnknown.getId());
        assertThat(firstResult.unknownWord()).isEqualTo(firstUnknown.getUnknownWord());

        assertThat(secondResult.id()).isEqualTo(secondUnknown.getId());
        assertThat(secondResult.unknownWord()).isEqualTo(secondUnknown.getUnknownWord());
    }

    @Test
    void newTranslation_validInputNotInDB() {
        NewTranslationRequest request = new NewTranslationRequest(
                "żółty",
                "yellow"
        );

        String result = appService.newTranslation(request);

        assertThat(result).isEqualTo("New translation added to the dictionary");
    }

    @Test
    void newTranslation_providedTranslationAlreadyExists() {
        Translation translation = new Translation("kot", "cat");
        NewTranslationRequest request = new NewTranslationRequest(
                "kot",
                "cat"
        );
        when(iTranslationsRepository.findByEnglishWordIgnoreCase(request.englishWord()))
                .thenReturn(Optional.of(translation));

        String result = appService.newTranslation(request);

        assertThat(result).isEqualTo("Provided translation already exists");
    }

    @Test
    void listTranslations_test() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id"));

        List<Translation> translations = new ArrayList<>();
        translations.add(new Translation("kot", "cat"));
        translations.add(new Translation("pies", "dog"));
        translations.add(new Translation("chomik", "hamster"));

        Page<Translation> page = new PageImpl<>(translations, pageable, translations.size());

        when(iTranslationsRepository.findAll(pageable)).thenReturn(page);

        List<TranslationResponse> translationResponses = appService.listTranslations(pageable);

        assertThat(translationResponses.size()).isEqualTo(3);
    }

}
