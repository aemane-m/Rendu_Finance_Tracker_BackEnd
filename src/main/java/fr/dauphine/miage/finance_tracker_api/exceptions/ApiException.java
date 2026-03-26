package fr.dauphine.miage.finance_tracker_api.exceptions;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
