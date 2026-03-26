package fr.dauphine.miage.finance_tracker_api.exceptions;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message);
    }
}
