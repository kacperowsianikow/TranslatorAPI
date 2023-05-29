package com.example.Translator.repository;

import com.example.Translator.translation.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITranslationsRepository extends JpaRepository<Translation, Long> {
    Optional<Translation> findByPolishWordIgnoreCase(String polishWord);
    Optional<Translation> findByEnglishWordIgnoreCase(String englishWord);

}
