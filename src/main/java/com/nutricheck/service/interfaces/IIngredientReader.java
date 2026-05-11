package com.nutricheck.service.interfaces;

import com.nutricheck.entity.Ingredient;
import java.util.List;
import java.util.Optional;

public interface IIngredientReader {

    Optional<Ingredient> findByName(String name);

    Ingredient getById(Long id);

    List<Ingredient> getAll();
}