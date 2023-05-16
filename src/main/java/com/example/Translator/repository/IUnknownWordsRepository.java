package com.example.Translator.repository;

import com.example.Translator.unknownwords.UnknownWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUnknownWordsRepository extends JpaRepository<UnknownWord, Long> {
    Optional<UnknownWord> findAllByUnknownWord(String unknownWord);

}
