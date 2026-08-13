package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.dto.request.CloseShiftRequest;
import com.joven.inventory.dto.request.OpenShiftRequest;
import com.joven.inventory.dto.response.ShiftResponse;
import com.joven.inventory.dto.response.ShiftSummaryResponse;
import com.joven.inventory.entity.Sale;
import com.joven.inventory.entity.SalePayment;
import com.joven.inventory.entity.Shift;
import com.joven.inventory.enums.PaymentMethod;
import com.joven.inventory.enums.SaleStatus;
import com.joven.inventory.enums.ShiftStatus;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.repository.SalePaymentRepository;
import com.joven.inventory.repository.SaleRepository;
import com.joven.inventory.repository.ShiftRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ShiftServiceImpl}.
 * Verifies cashier shift lifecycle: opening, closing with reconciliation, and current shift retrieval.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShiftServiceImpl Unit Tests")
class ShiftServiceImplTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SalePaymentRepository salePaymentRepository;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    @BeforeEach
    void setUp() {
        AuditContext.set("testuser", "127.0.0.1");
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
    }

    // ========================================================================
    // openShift tests
    // ========================================================================

    @Test
    @DisplayName("openShift - given no open shift - creates new shift")
    void openShift_givenNoOpenShift_createsNewShift() {
        // Arrange
        when(shiftRepository.findByCashierAndStatus("testuser", ShiftStatus.OPEN))
                .thenReturn(Optional.empty());
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> {
            Shift shift = invocation.getArgument(0);
            shift.setId(1L);
            return shift;
        });

        OpenShiftRequest request = OpenShiftRequest.builder()
                .openingAmount(new BigDecimal("5000.00"))
                .build();

        // Act
        ShiftResponse response = shiftService.openShift(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ShiftStatus.OPEN.name());
        assertThat(response.getCashier()).isEqualTo("testuser");
        assertThat(response.getOpeningAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));

        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    @DisplayName("openShift - given existing open shift - throws BusinessRuleException")
    void openShift_givenExistingOpenShift_throwsBusinessRuleException() {
        // Arrange
        Shift existingShift = createTestShift(1L, "testuser", ShiftStatus.OPEN, new BigDecimal("3000.00"));

        when(shiftRepository.findByCashierAndStatus("testuser", ShiftStatus.OPEN))
                .thenReturn(Optional.of(existingShift));

        OpenShiftRequest request = OpenShiftRequest.builder()
                .openingAmount(new BigDecimal("5000.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> shiftService.openShift(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has an open shift");
    }

    // ========================================================================
    // closeShift tests
    // ========================================================================

    @Test
    @DisplayName("closeShift - given open shift - closes with reconciliation")
    void closeShift_givenOpenShift_closesWithReconciliation() {
        // Arrange
        Shift shift = createTestShift(1L, "testuser", ShiftStatus.OPEN, new BigDecimal("1000.00"));

        Sale sale1 = createTestSale(1L, SaleStatus.PAID, new BigDecimal("500.00"));
        Sale sale2 = createTestSale(2L, SaleStatus.PAID, new BigDecimal("500.00"));
        List<Sale> shiftSales = List.of(sale1, sale2);

        SalePayment payment1 = createTestPayment(1L, sale1, PaymentMethod.CASH, new BigDecimal("500.00"));
        SalePayment payment2 = createTestPayment(2L, sale2, PaymentMethod.CASH, new BigDecimal("500.00"));

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(saleRepository.findByShiftId(1L)).thenReturn(shiftSales);
        when(salePaymentRepository.findBySaleId(1L)).thenReturn(List.of(payment1));
        when(salePaymentRepository.findBySaleId(2L)).thenReturn(List.of(payment2));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CloseShiftRequest request = CloseShiftRequest.builder()
                .closingAmount(new BigDecimal("2000.00"))
                .build();

        // Act
        ShiftSummaryResponse response = shiftService.closeShift(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ShiftStatus.CLOSED.name());
        assertThat(response.getTotalSales()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getTotalTransactions()).isEqualTo(2);

        // Expected = opening(1000) + cashSales(1000) - change(0) = 2000
        assertThat(response.getExpectedAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        // Difference = closingAmount(2000) - expected(2000) = 0
        assertThat(response.getDifference()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("closeShift - given already closed shift - throws BusinessRuleException")
    void closeShift_givenAlreadyClosedShift_throwsBusinessRuleException() {
        // Arrange
        Shift shift = createTestShift(1L, "testuser", ShiftStatus.CLOSED, new BigDecimal("1000.00"));

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        CloseShiftRequest request = CloseShiftRequest.builder()
                .closingAmount(new BigDecimal("2000.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> shiftService.closeShift(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already CLOSED");
    }

    @Test
    @DisplayName("closeShift - given different user - throws BusinessRuleException")
    void closeShift_givenDifferentUser_throwsBusinessRuleException() {
        // Arrange
        Shift shift = createTestShift(1L, "otheruser", ShiftStatus.OPEN, new BigDecimal("1000.00"));

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        CloseShiftRequest request = CloseShiftRequest.builder()
                .closingAmount(new BigDecimal("2000.00"))
                .build();

        // Act & Assert (AuditContext is "testuser" but shift belongs to "otheruser")
        assertThatThrownBy(() -> shiftService.closeShift(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not 'testuser'");
    }

    // ========================================================================
    // getCurrentShift tests
    // ========================================================================

    @Test
    @DisplayName("getCurrentShift - given open shift - returns shift response")
    void getCurrentShift_givenOpenShift_returnsShiftResponse() {
        // Arrange
        Shift shift = createTestShift(1L, "testuser", ShiftStatus.OPEN, new BigDecimal("5000.00"));

        when(shiftRepository.findCurrentOpenShift("testuser"))
                .thenReturn(Optional.of(shift));

        // Act
        ShiftResponse response = shiftService.getCurrentShift();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCashier()).isEqualTo("testuser");
        assertThat(response.getStatus()).isEqualTo(ShiftStatus.OPEN.name());
        assertThat(response.getOpeningAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("getCurrentShift - given no open shift - throws ResourceNotFoundException")
    void getCurrentShift_givenNoOpenShift_throwsResourceNotFoundException() {
        // Arrange
        when(shiftRepository.findCurrentOpenShift("testuser"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> shiftService.getCurrentShift())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No open shift found");
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private Shift createTestShift(Long id, String cashier, ShiftStatus status, BigDecimal openingAmount) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setCashier(cashier);
        shift.setStatus(status);
        shift.setOpeningAmount(openingAmount);
        shift.setOpenedAt(LocalDateTime.now());
        return shift;
    }

    private Sale createTestSale(Long id, SaleStatus status, BigDecimal totalAmount) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setSaleNo("RCT-202608-" + String.format("%05d", id));
        sale.setStatus(status);
        sale.setTotalAmount(totalAmount);
        sale.setChangeAmount(BigDecimal.ZERO);
        sale.setCreatedBy("testuser");
        sale.setSaleDate(LocalDateTime.now());
        return sale;
    }

    private SalePayment createTestPayment(Long id, Sale sale, PaymentMethod method, BigDecimal amount) {
        SalePayment payment = new SalePayment();
        payment.setId(id);
        payment.setSale(sale);
        payment.setPaymentMethod(method);
        payment.setAmount(amount);
        return payment;
    }
}
