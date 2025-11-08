package com.example.access_guard.exception;

public class DifferentPasswordsException extends RuntimeException{
    public DifferentPasswordsException(String message) {super(message);}
}
