package com.nutricheck.exceptions;

public class ScanNotFoundException extends RuntimeException{
    public ScanNotFoundException(String message){
        super(message);
    }
}
