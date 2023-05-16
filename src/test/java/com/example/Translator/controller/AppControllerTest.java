package com.example.Translator.controller;

import com.example.Translator.report.Report;
import com.example.Translator.service.AppService;
import com.example.Translator.service.ReportService;
import com.example.Translator.translation.NewTranslationRequest;
import com.example.Translator.translation.SingleTranslationResponse;
import com.example.Translator.translation.TranslationResponse;
import com.example.Translator.unknownwords.UnknownWordResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
@WebMvcTest(AppController.class)
class AppControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AppService appService;
    @MockBean
    private ReportService reportService;

    @Test
    void translate_test() throws Exception {
        String input = "cat";
        String translation = "kot";

        SingleTranslationResponse expected = new SingleTranslationResponse(translation);

        when(appService.translateWord(input)).thenReturn(expected);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/api/translator/translate")
                .param("word", input);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"translatedWord\":\"kot\"}"));

        verify(appService).translateWord(input);
    }

    @Test
    void translateSentence_test() throws Exception {
        String input = "To jest duży kot";
        String translated = "This is big cat";
        when(appService.translateSentence(input)).thenReturn(translated);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/api/translator/translate-sentence")
                .param("sentence", input);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().string(translated));

        verify(appService).translateSentence(input);
    }

    @Test
    void listUnknownWords() throws Exception {
        UnknownWordResponse firstUnknown = new UnknownWordResponse(1L, "dom");
        UnknownWordResponse secondUnknown = new UnknownWordResponse(2L, "kamienica");
        List<UnknownWordResponse> unknownWords = Arrays.asList(firstUnknown, secondUnknown);

        when(appService.listUnknownWords()).thenReturn(unknownWords);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/api/translator/unknown-words");

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unknownWord").value("dom"))
                .andExpect(jsonPath("$[1].unknownWord").value("kamienica"));

        verify(appService).listUnknownWords();
    }

    @Test
    void report_test() throws Exception {
        Map<Integer, Long> wordsPolish = new HashMap<>();
        wordsPolish.put(3, 2L);
        wordsPolish.put(4, 3L);

        Map<Integer, Long> wordsEnglish = new HashMap<>();
        wordsEnglish.put(3, 4L);
        wordsEnglish.put(5, 1L);

        Map<String, Map<Integer, Long>> wordsLength = new HashMap<>();
        wordsLength.put("POL", wordsPolish);
        wordsLength.put("ENG", wordsEnglish);

        Map<String, Double> averageLength = new HashMap<>();
        averageLength.put("POL", 3.62);
        averageLength.put("ENG", 3.41);

        Report report = new Report(
                10L,
                wordsLength,
                averageLength,
                2L
                );

        when(reportService.report()).thenReturn(report);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/api/translator/report")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\n" +
                        "    \"wordsNumber\": 10,\n" +
                        "    \"wordsLength\": {\n" +
                        "        \"POL\": {\n" +
                        "            \"3\": 2,\n" +
                        "            \"4\": 3\n" +
                        "        },\n" +
                        "        \"ENG\": {\n" +
                        "            \"3\": 4,\n" +
                        "            \"5\": 1\n" +
                        "        }\n" +
                        "    },\n" +
                        "    \"averageLength\": {\n" +
                        "        \"POL\": 3.62,\n" +
                        "        \"ENG\": 3.41\n" +
                        "    },\n" +
                        "    \"unknownWordsNumber\": 2\n" +
                        "}"
                ));

        verify(reportService).report();
    }

    @Test
    void listDictionary_test() throws Exception {
        List<TranslationResponse> list = new ArrayList<>();
        list.add(new TranslationResponse(1L, "pies", "dog"));
        list.add(new TranslationResponse(2L, "kot", "cat"));

        when(appService.listTranslations(any(Pageable.class))).thenReturn(list);

        int pageNumber = 0;
        int pageSize = 5;
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/api/translator/list-dictionary")
                .param("pageNumber", String.valueOf(pageNumber))
                .param("pageSize", String.valueOf(pageSize))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().json("[\n" +
                        "    {\n" +
                        "        \"id\": 1,\n" +
                        "        \"polishWord\": \"pies\",\n" +
                        "        \"englishWord\": \"dog\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"id\": 2,\n" +
                        "        \"polishWord\": \"kot\",\n" +
                        "        \"englishWord\": \"cat\"\n" +
                        "    }\n" +
                        "]"
                ));

        verify(appService).listTranslations(any(Pageable.class));
    }

    @Test
    void newTranslation_test() throws Exception {
        NewTranslationRequest request = new NewTranslationRequest(
                "płot",
                "fence"
        );
        String response = "New translation added to the dictionary";

        when(appService.newTranslation(request))
                .thenReturn(response);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/api/translator/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"polishWord\": \"płot\",\n" +
                        "    \"englishWord\": \"fence\"\n" +
                        "}")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().string(response));

        verify(appService).newTranslation(request);
    }

    @Test
    void newTranslation_testInvalidInput() throws Exception {
        NewTranslationRequest request = new NewTranslationRequest(
                "gwiazda777",
                "star"
        );

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/api/translator/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(request))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

}