
package dev.walid.tax;

import java.math.BigDecimal;
import java.util.List;

public record TaxResult(
        BigDecimal grossIncome,
        BigDecimal deductions,
        BigDecimal taxableIncome,
        BigDecimal estimatedFederalTaxBeforeCredits,
        BigDecimal basicPersonalAmount,
        BigDecimal basicPersonalAmountCreditApplied,
        BigDecimal estimatedFederalTaxAfterCredits,
        List<BracketLine> breakdown) {

    public TaxResult {
        breakdown = List.copyOf(breakdown);
    }

    public record BracketLine(
            BigDecimal incomeInBracket,
            BigDecimal rate,
            BigDecimal taxInBracket) {}
}
