package com.example.Translator.report;

import com.example.Translator.repository.ITranslationsRepository;
import com.example.Translator.repository.IUnknownWordsRepository;
import com.example.Translator.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock
    private ITranslationsRepository iTranslationsRepository;
    @Mock
    private IUnknownWordsRepository iUnknownWordsRepository;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        iTranslationsRepository = mock(ITranslationsRepository.class);
        iUnknownWordsRepository = mock(IUnknownWordsRepository.class);
        reportService = new ReportService(iTranslationsRepository, iUnknownWordsRepository);
    }

    @Test
    public void report_test() {
        when(iTranslationsRepository.count()).thenReturn(10L);
        when(iUnknownWordsRepository.count()).thenReturn(5L);

        Report report = reportService.report();

        assertThat(report).isNotNull();
        assertThat(report.wordsLength()).isNotNull();

        assertThat(report.wordsNumber()).isEqualTo(10L);
        assertThat(report.unknownWordsNumber()).isEqualTo(5L);

        assertThat(report.wordsLength().containsKey("POL")).isTrue();
        assertThat(report.wordsLength().containsKey("ENG")).isTrue();

        assertThat(report.averageLength().containsKey("POL")).isTrue();
        assertThat(report.averageLength().containsKey("ENG")).isTrue();
    }

}