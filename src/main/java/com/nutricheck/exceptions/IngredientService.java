package com.nutricheck.exceptions;

public class IngredientService extends RuntimeException {
    public IngredientService(String message) {
        super(message);
    }
}