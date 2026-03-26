package fr.dauphine.miage.finance_tracker_api.mapper;

import fr.dauphine.miage.finance_tracker_api.dto.TransactionResponse;
import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType(),
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getLabel(),
                t.getDate(),
                t.getNote(),
                t.getSource(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
