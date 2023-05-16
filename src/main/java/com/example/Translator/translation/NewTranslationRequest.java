package com.example.Translator.translation;

import jakarta.validation.constraints.Pattern;

public record NewTranslationRequest(
        @Pattern(regexp = "^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$",
                message = "Input cannot contain spaces, numbers or special characters"
        )
        String polishWord,
        @Pattern(regexp = "^[a-zA-Z]+$",
                message = "Input cannot contain spaces, numbers or special characters"
        )
        String englishWord) {

}
