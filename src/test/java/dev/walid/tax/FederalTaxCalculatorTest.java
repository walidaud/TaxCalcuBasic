package dev.walid.tax;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FederalTaxCalculatorTest {
    private FederalTaxCalculator calculator;

    @BeforeEach
    void setUp() {
        // Small, intentionally simple brackets make expected values easy to verify.
        calculator = new FederalTaxCalculator(List.of(
                new TaxBracket(new BigDecimal("10000"), new BigDecimal("0.10")),
                new TaxBracket(new BigDecimal("20000"), new BigDecimal("0.20")),
                new TaxBracket(null, new BigDecimal("0.30"))
        ));
    }

    @Test
    void calculatesIncomeWithinFirstBracket() {
        TaxResult result = calculator.calculate(new BigDecimal("5000"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("500.00"), result.estimatedFederalTaxBeforeCredits());
    }

    @Test
    void calculatesAcrossMultipleBrackets() {
        TaxResult result = calculator.calculate(new BigDecimal("15000"), BigDecimal.ZERO);
        // 10,000 * 10% + 5,000 * 20%
        assertEquals(new BigDecimal("2000.00"), result.estimatedFederalTaxBeforeCredits());
        assertEquals(2, result.breakdown().size());
    }

    @Test
    void exactBracketBoundaryDoesNotTaxIncomeTwice() {
        TaxResult result = calculator.calculate(new BigDecimal("10000"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("1000.00"), result.estimatedFederalTaxBeforeCredits());
        assertEquals(1, result.breakdown().size());
    }

    @Test
    void deductionsReduceTaxableIncome() {
        TaxResult result = calculator.calculate(new BigDecimal("15000"), new BigDecimal("5000"));
        assertEquals(new BigDecimal("10000.00"), result.taxableIncome());
        assertEquals(new BigDecimal("1000.00"), result.estimatedFederalTaxBeforeCredits());
    }

    @Test
    void deductionsCannotMakeTaxableIncomeNegative() {
        TaxResult result = calculator.calculate(new BigDecimal("1000"), new BigDecimal("2000"));
        assertEquals(new BigDecimal("0.00"), result.taxableIncome());
        assertEquals(new BigDecimal("0.00"), result.estimatedFederalTaxBeforeCredits());
    }

    @Test
    void rejectsNegativeIncome() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(new BigDecimal("-1"), BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeDeductions() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(BigDecimal.ONE, new BigDecimal("-1")));
    }

    @Test
    void rejectsInvalidBracketOrdering() {
        assertThrows(IllegalArgumentException.class, () -> new FederalTaxCalculator(List.of(
                new TaxBracket(new BigDecimal("20000"), new BigDecimal("0.10")),
                new TaxBracket(new BigDecimal("10000"), new BigDecimal("0.20")),
                new TaxBracket(null, new BigDecimal("0.30"))
        )));
    }
}
