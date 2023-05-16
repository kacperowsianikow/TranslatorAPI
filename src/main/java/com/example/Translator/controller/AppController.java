package com.example.Translator.controller;

import com.example.Translator.report.Report;
import com.example.Translator.service.AppService;
import com.example.Translator.service.ReportService;
import com.example.Translator.translation.NewTranslationRequest;
import com.example.Translator.translation.SingleTranslationResponse;
import com.example.Translator.translation.TranslationResponse;
import com.example.Translator.unknownwords.UnknownWordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/translator")
@RequiredArgsConstructor
@Slf4j
public class AppController {
    private final AppService appService;
    private final ReportService reportService;

    @GetMapping("/translate")
    public SingleTranslationResponse translateWord(@RequestParam("word") String word) {
        return appService.translateWord(word);
    }

    @GetMapping("/translate-sentence")
    public String translateSentence(@RequestParam("sentence") String sentence) {
        return appService.translateSentence(sentence);
    }

    @GetMapping("/unknown-words")
    public List<UnknownWordResponse> listUnknownWords() {
        return appService.listUnknownWords();
    }

    @GetMapping("/report")
    public Report reportResponse() {
        return reportService.report();
    }

    @GetMapping("/list-dictionary")
    public List<TranslationResponse> listTranslations(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize) {
        log.info("Received request to list dictionary, pageNumber={}, pageSize={}",
                pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id"));

        return appService.listTranslations(pageable);
    }

    @PostMapping("/new")
    public String newTranslation(@Valid @RequestBody NewTranslationRequest newTranslationRequest) {
        return appService.newTranslation(newTranslationRequest);
    }

}
