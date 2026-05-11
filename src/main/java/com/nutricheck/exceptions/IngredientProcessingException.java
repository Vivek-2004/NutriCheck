package com.nutricheck.exceptions;

public class IngredientProcessingException extends RuntimeException {
    public IngredientProcessingException(String message) {
        super(message);
    }
    public IngredientProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}