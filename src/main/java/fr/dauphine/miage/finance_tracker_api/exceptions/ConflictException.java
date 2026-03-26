package fr.dauphine.miage.finance_tracker_api.exceptions;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message);
    }
}
