package com.example.Translator.service;

import com.example.Translator.dto.ReportDTO;
import com.example.Translator.mapper.Mapper;
import com.example.Translator.report.Report;
import com.example.Translator.repository.ITranslationsRepository;
import com.example.Translator.repository.IUnknownWordsRepository;
import com.example.Translator.translation.Translation;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ITranslationsRepository iTranslationsRepository;
    private final IUnknownWordsRepository iUnknownWordsRepository;
    private final Mapper mapper;

    public ReportDTO report() {
        List<Translation> translations = iTranslationsRepository.findAll();

        Map<String, Map<Integer, Long>> wordsLengths = new HashMap<>();
        wordsLengths.put("POL", countWordsLengths(translations, Translation::getPolishWord));
        wordsLengths.put("ENG", countWordsLengths(translations, Translation::getEnglishWord));

        double averagePolishLen = calculateAverageLength(translations, Translation::getPolishWord);
        double averageEnglishLen = calculateAverageLength(translations, Translation::getEnglishWord);

        DecimalFormat df = new DecimalFormat("#.00");
        Map<String, Double> averageLengths = new HashMap<>();
        averageLengths.put("POL", Double.parseDouble(df.format(averagePolishLen)));
        averageLengths.put("ENG", Double.parseDouble(df.format(averageEnglishLen)));

        Report report = new Report(
                iTranslationsRepository.count(),
                wordsLengths,
                averageLengths,
                iUnknownWordsRepository.count()
        );

        return mapper.toReportDTO(report);
    }

    private Map<Integer, Long> countWordsLengths(List<Translation> translations,
                                                 Function<Translation, String> extractWords) {
        return translations.stream()
                .map(extractWords)
                .collect(Collectors.groupingBy(String::length,
                        Collectors.counting()
                ));
    }

    private double calculateAverageLength(List<Translation> translations,
                                          Function<Translation, String> extractWords) {
        return translations.stream()
                .mapToInt(translation -> extractWords.apply(translation).length())
                .average()
                .orElse(0.0);
    }

    public byte[] generateReportInPdf() throws Exception {
        ReportDTO reportDTO = report();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        log.info("Converting to PDF");

        document.add(new Paragraph("1. Number of words: " + reportDTO.wordsNumber()));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("2. Words of different lengths:"));
        document.add(new Paragraph("2. 1. Number of words of different lengths in polish"));
        document.add(new Paragraph("(no of letters: no of words of this length):"));
        for (Map.Entry<Integer, Long> entry : reportDTO.wordsLength().get("POL").entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
        }
        document.add(new Paragraph(" "));
        document.add(new Paragraph("2. 2. Number of words of different lengths in english"));
        document.add(new Paragraph("(no of letters: no of words of this length):"));
        for (Map.Entry<Integer, Long> entry : reportDTO.wordsLength().get("ENG").entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
        }
        document.add(new Paragraph(" "));

        document.add(new Paragraph("3. Average length of words in diff. languages: "));
        document.add(new Paragraph("Polish: " + reportDTO.averageLength().get("POL")));
        document.add(new Paragraph("English: " + reportDTO.averageLength().get("ENG")));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("4. Number of unknown words: " + reportDTO.unknownWordsNumber()));

        document.close();

        return outputStream.toByteArray();
    }

}
