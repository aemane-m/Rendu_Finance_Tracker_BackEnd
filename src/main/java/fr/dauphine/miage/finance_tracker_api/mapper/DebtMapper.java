package fr.dauphine.miage.finance_tracker_api.mapper;

import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtResponse;
import fr.dauphine.miage.finance_tracker_api.models.Debt;
import fr.dauphine.miage.finance_tracker_api.models.DebtPayment;
import org.springframework.stereotype.Component;

@Component
public class DebtMapper {

    public DebtResponse toResponse(Debt debt) {
        return new DebtResponse(
                debt.getId(),
                debt.getCreditorName(),
                debt.getTotalAmount(),
                debt.getRemainingAmount(),
                debt.getMonthlyPayment(),
                debt.getInterestRate(),
                debt.getStartDate(),
                debt.getDueDate(),
                debt.getStatus().name(),
                debt.getCreatedAt(),
                debt.getUpdatedAt()
        );
    }

    public DebtPaymentResponse toPaymentResponse(DebtPayment payment) {
        return new DebtPaymentResponse(
                payment.getId(),
                payment.getDebt().getId(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getNote(),
                payment.getTransaction() != null ? payment.getTransaction().getId() : null,
                payment.getCreatedAt()
        );
    }
}
