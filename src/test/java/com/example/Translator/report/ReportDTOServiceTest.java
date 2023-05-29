package com.example.Translator.report;

import com.example.Translator.dto.ReportDTO;
import com.example.Translator.mapper.Mapper;
import com.example.Translator.repository.ITranslationsRepository;
import com.example.Translator.repository.IUnknownWordsRepository;
import com.example.Translator.service.ReportService;
import com.example.Translator.translation.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ReportDTOServiceTest {
    @Mock
    private ITranslationsRepository iTranslationsRepository;
    @Mock
    private IUnknownWordsRepository iUnknownWordsRepository;
    @Mock
    private Mapper mapper;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                iTranslationsRepository,
                iUnknownWordsRepository,
                mapper
        );
    }

    @Test
    public void report_test() {
        List<Translation> translations = Arrays.asList(
                new Translation("pies", "dog"),
                new Translation("kot", "cat")
        );

        Map<Integer, Long> wordsLengthsCountInPol = new HashMap<>();
        wordsLengthsCountInPol.put(3, 1L);
        wordsLengthsCountInPol.put(4, 1L);

        Map<Integer, Long> wordsLengthsCountEng = new HashMap<>();
        wordsLengthsCountEng.put(3, 2L);

        Map<String, Map<Integer, Long>> expectedWordsLengths = new HashMap<>();
        expectedWordsLengths.put("POL", wordsLengthsCountInPol);
        expectedWordsLengths.put("ENG", wordsLengthsCountEng);

        Map<String, Double> expectedAverageLengths = new HashMap<>();
        expectedAverageLengths.put("POL", 3.50);
        expectedAverageLengths.put("ENG", 3.00);

        Report expectedReport = new Report(
                5L,
                expectedWordsLengths,
                expectedAverageLengths,
                2L
        );

        ReportDTO expectedReportDTO = new ReportDTO(
                5L,
                expectedWordsLengths,
                expectedAverageLengths,
                2L
        );

        when(iTranslationsRepository.findAll())
                .thenReturn(translations);
        when(iTranslationsRepository.count())
                .thenReturn(5L);
        when(iUnknownWordsRepository.count())
                .thenReturn(2L);
        when(mapper.toReportDTO(expectedReport))
                .thenReturn(expectedReportDTO);

        ReportDTO result = reportService.report();

        assertThat(result).isEqualTo(expectedReportDTO);
    }

}