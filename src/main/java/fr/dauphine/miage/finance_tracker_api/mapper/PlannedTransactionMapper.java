package fr.dauphine.miage.finance_tracker_api.mapper;

import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionRunResponse;
import fr.dauphine.miage.finance_tracker_api.models.PlannedTransaction;
import fr.dauphine.miage.finance_tracker_api.models.PlannedTransactionRun;
import org.springframework.stereotype.Component;

@Component
public class PlannedTransactionMapper {

    public PlannedTransactionResponse toResponse(PlannedTransaction pt) {
        return new PlannedTransactionResponse(
                pt.getId(),
                pt.getLabel(),
                pt.getAmount(),
                pt.getType().name(),
                pt.getCategory() != null ? pt.getCategory().getId() : null,
                pt.getCategory() != null ? pt.getCategory().getName() : null,
                pt.getFrequency().name(),
                pt.getNextDueDate(),
                pt.isAutoCreate(),
                pt.isActive(),
                pt.getCreatedAt(),
                pt.getUpdatedAt()
        );
    }

    public PlannedTransactionRunResponse toRunResponse(PlannedTransactionRun run) {
        return new PlannedTransactionRunResponse(
                run.getId(),
                run.getPlannedTransaction().getId(),
                run.getDueDate(),
                run.getExecutedAt(),
                run.getStatus().name(),
                run.getCreatedTransaction() != null ? run.getCreatedTransaction().getId() : null,
                run.getErrorMessage()
        );
    }
}
