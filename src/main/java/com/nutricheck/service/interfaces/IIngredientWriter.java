package com.nutricheck.service.interfaces;

import com.nutricheck.entity.Ingredient;
import java.util.List;

public interface IIngredientWriter {
    Ingredient save(Ingredient ingredient);
    // Find existing or create new ingredient
    // Prevents duplicate ingredients in DB
    Ingredient getOrCreate(
            String name,
            String description,
            String category,
            String riskLevel,
            List<String> sideEffects
    );
}