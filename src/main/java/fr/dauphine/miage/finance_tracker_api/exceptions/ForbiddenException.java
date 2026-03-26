package fr.dauphine.miage.finance_tracker_api.exceptions;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message);
    }
}
