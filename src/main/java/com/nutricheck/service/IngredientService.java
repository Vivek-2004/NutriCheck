package com.nutricheck.service;

import com.nutricheck.entity.Ingredient;
import com.nutricheck.exceptions.IngredientProcessingException;
import com.nutricheck.repository.IngredientRepository;
import com.nutricheck.service.interfaces.IIngredientReader;
import com.nutricheck.service.interfaces.IIngredientWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService implements IIngredientReader, IIngredientWriter {

    private final IngredientRepository ingredientRepository;

    // ============ IIngredientReader ============

    @Override
    public Optional<Ingredient> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        return ingredientRepository.findByNameIgnoreCase(name.trim());
    }

    @Override
    public Ingredient getById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientProcessingException(
                        "Ingredient not found with id: " + id));
    }

    @Override
    public List<Ingredient> getAll() {
        return ingredientRepository.findAll();
    }

    // ============ IIngredientWriter ============

    @Override
    @Transactional
    public Ingredient save(Ingredient ingredient) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient cannot be null");
        }
        return ingredientRepository.save(ingredient);
    }

    @Override
    @Transactional
    public Ingredient getOrCreate(
            String name,
            String description,
            String category,
            String riskLevel,
            List<String> sideEffects
    ) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }

        // Check if ingredient already exists (prevents duplicates)
        return ingredientRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    // Create new ingredient
                    String sideEffectsStr = (sideEffects != null && !sideEffects.isEmpty())
                            ? String.join(", ", sideEffects)
                            : null;

                    Ingredient newIngredient = Ingredient.builder()
                            .name(name.trim())
                            .description(description)
                            .category(category)
                            .riskLevel(riskLevel)
                            .sideEffects(sideEffectsStr)
                            .build();

                    Ingredient saved = ingredientRepository.save(newIngredient);
                    log.info("Created new ingredient: {} (Risk: {})", name, riskLevel);
                    return saved;
                });
    }
}