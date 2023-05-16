package com.example.Translator.unknownwords;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnknownWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String unknownWord;

    public UnknownWord(String unknownWord) {
        this.unknownWord = unknownWord;
    }

}
