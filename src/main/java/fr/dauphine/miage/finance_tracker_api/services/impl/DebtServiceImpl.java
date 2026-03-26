package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.CreationDebtRequest;
import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentRequest;
import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateDebtRequest;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.mapper.DebtMapper;
import fr.dauphine.miage.finance_tracker_api.models.Debt;
import fr.dauphine.miage.finance_tracker_api.models.DebtPayment;
import fr.dauphine.miage.finance_tracker_api.models.DebtStatus;
import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import fr.dauphine.miage.finance_tracker_api.repository.DebtPaymentRepository;
import fr.dauphine.miage.finance_tracker_api.repository.DebtRepository;
import fr.dauphine.miage.finance_tracker_api.repository.TransactionRepository;
import fr.dauphine.miage.finance_tracker_api.services.DebtService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final DebtMapper debtMapper;

    public DebtServiceImpl(DebtRepository debtRepository,
                            DebtPaymentRepository debtPaymentRepository,
                            TransactionRepository transactionRepository,
                            DebtMapper debtMapper) {
        this.debtRepository = debtRepository;
        this.debtPaymentRepository = debtPaymentRepository;
        this.transactionRepository = transactionRepository;
        this.debtMapper = debtMapper;
    }

    @Override
    public List<DebtResponse> findAll(UUID userId) {
        return debtRepository.findByUserId(userId)
                .stream()
                .map(debtMapper::toResponse)
                .toList();
    }

    @Override
    public DebtResponse findById(Long id, UUID userId) {
        Debt debt = debtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found: " + id));
        return debtMapper.toResponse(debt);
    }

    @Override
    @Transactional
    public DebtResponse create(CreationDebtRequest request, UUID userId) {
        Instant now = Instant.now();
        Debt debt = Debt.builder()
                .userId(userId)
                .creditorName(request.creditorName().trim())
                .totalAmount(request.totalAmount())
                .remainingAmount(request.totalAmount())
                .monthlyPayment(request.monthlyPayment())
                .interestRate(request.interestRate())
                .startDate(request.startDate())
                .dueDate(request.dueDate())
                .status(DebtStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return debtMapper.toResponse(debtRepository.save(debt));
    }

    @Override
    @Transactional
    public DebtResponse update(Long id, UpdateDebtRequest request, UUID userId) {
        Debt debt = debtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found: " + id));

        if (debt.getStatus() != DebtStatus.ACTIVE) {
            throw new BadRequestException("Only active debts can be updated");
        }

        debt.setCreditorName(request.creditorName().trim());
        debt.setTotalAmount(request.totalAmount());
        debt.setMonthlyPayment(request.monthlyPayment());
        debt.setInterestRate(request.interestRate());
        debt.setStartDate(request.startDate());
        debt.setDueDate(request.dueDate());
        debt.setUpdatedAt(Instant.now());

        return debtMapper.toResponse(debtRepository.save(debt));
    }

    @Override
    @Transactional
    public DebtResponse registerPayment(Long id, DebtPaymentRequest request, UUID userId) {
        Debt debt = debtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found: " + id));

        if (debt.getStatus() != DebtStatus.ACTIVE) {
            throw new BadRequestException("Cannot register a payment on a non-active debt");
        }

        if (request.amount().compareTo(debt.getRemainingAmount()) > 0) {
            throw new BadRequestException("Payment amount exceeds remaining debt amount");
        }

        Transaction transaction = null;
        if (request.transactionId() != null) {
            transaction = transactionRepository.findByIdAndUserId(request.transactionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + request.transactionId()));
        }

        DebtPayment payment = DebtPayment.builder()
                .debt(debt)
                .userId(userId)
                .paymentDate(request.paymentDate())
                .amount(request.amount())
                .note(request.note())
                .transaction(transaction)
                .createdAt(Instant.now())
                .build();
        debtPaymentRepository.save(payment);

        BigDecimal newRemaining = debt.getRemainingAmount().subtract(request.amount());
        debt.setRemainingAmount(newRemaining);
        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(DebtStatus.PAID);
        }
        debt.setUpdatedAt(Instant.now());

        return debtMapper.toResponse(debtRepository.save(debt));
    }

    @Override
    public Page<DebtPaymentResponse> findPayments(Long debtId, UUID userId, Pageable pageable) {
        debtRepository.findByIdAndUserId(debtId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found: " + debtId));

        return debtPaymentRepository.findByDebtId(debtId, pageable)
                .map(debtMapper::toPaymentResponse);
    }

    @Override
    public DebtSummaryResponse summary(UUID userId) {
        List<Debt> debts = debtRepository.findByUserId(userId);

        List<Debt> activeDebts = debts.stream()
                .filter(d -> d.getStatus() == DebtStatus.ACTIVE)
                .toList();

        long totalPaid = debts.stream()
                .filter(d -> d.getStatus() == DebtStatus.PAID)
                .count();

        BigDecimal totalRemainingAmount = activeDebts.stream()
                .map(Debt::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMonthlyPayment = activeDebts.stream()
                .map(Debt::getMonthlyPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DebtSummaryResponse(
                activeDebts.size(),
                (int) totalPaid,
                totalRemainingAmount,
                totalMonthlyPayment
        );
    }

    @Override
    @Transactional
    public void delete(Long id, UUID userId) {
        Debt debt = debtRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found: " + id));
        debtRepository.delete(debt);
    }
}
