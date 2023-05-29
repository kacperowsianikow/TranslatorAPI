package com.example.Translator.dto;

import jakarta.validation.constraints.Pattern;

public record TranslationCreationDTO(
        @Pattern(regexp = "^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$",
                message = "Input cannot contain spaces, numbers or special characters"
        )
        String polishWord,
        @Pattern(regexp = "^[a-zA-Z]+$",
                message = "Input cannot contain spaces, numbers or special characters"
        )
        String englishWord) {

}
