package fr.dauphine.miage.finance_tracker_api.exceptions;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
