package dev.walid.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Estimates tax using configurable marginal brackets.
 *
 * This deliberately does not model tax credits, provincial/territorial tax,
 * surtaxes, special income rules, or other parts of a Canadian tax return.
 */
public final class FederalTaxCalculator {
    private static final int MONEY_SCALE = 2;
    private final List<TaxBracket> brackets;

    public FederalTaxCalculator(List<TaxBracket> brackets) {
        Objects.requireNonNull(brackets, "brackets must not be null");
        if (brackets.isEmpty()) {
            throw new IllegalArgumentException("At least one tax bracket is required");
        }

        List<TaxBracket> copy = List.copyOf(brackets);
        for (int i = 0; i < copy.size(); i++) {
            TaxBracket current = Objects.requireNonNull(copy.get(i), "bracket must not be null");
            boolean last = i == copy.size() - 1;
            if (last && current.upperBoundExclusive() != null) {
                throw new IllegalArgumentException("Last bracket must have no upper bound");
            }
            if (!last && current.upperBoundExclusive() == null) {
                throw new IllegalArgumentException("Only the last bracket can have no upper bound");
            }
            
            if (i > 0) {
                BigDecimal previous = copy.get(i - 1).upperBoundExclusive();
                BigDecimal currentUpperBound = current.upperBoundExclusive();

                if (previous == null || (currentUpperBound != null
                    && currentUpperBound.compareTo(previous) <= 0)) {
                    throw new IllegalArgumentException("Bracket upper bounds must increase");
                }
            }

        }
        this.brackets = copy;
    }

    public TaxResult calculate(BigDecimal grossIncome, BigDecimal deductions) {
        requireNonNegative(grossIncome, "gross income");
        requireNonNegative(deductions, "deductions");

        BigDecimal taxableIncome = grossIncome.subtract(deductions).max(BigDecimal.ZERO);
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal lowerBound = BigDecimal.ZERO;
        List<TaxResult.BracketLine> lines = new ArrayList<>();

        for (TaxBracket bracket : brackets) {
            if (taxableIncome.compareTo(lowerBound) <= 0) {
                break;
            }

            BigDecimal taxableCeiling = bracket.upperBoundExclusive() == null
                    ? taxableIncome
                    : taxableIncome.min(bracket.upperBoundExclusive());
            BigDecimal incomeInBracket = taxableCeiling.subtract(lowerBound).max(BigDecimal.ZERO);
            BigDecimal taxInBracket = incomeInBracket.multiply(bracket.rate())
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            if (incomeInBracket.signum() > 0) {
                lines.add(new TaxResult.BracketLine(
                        incomeInBracket.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                        bracket.rate(),
                        taxInBracket));
                tax = tax.add(taxInBracket);
            }

            if (bracket.upperBoundExclusive() == null) {
                break;
            }
            lowerBound = bracket.upperBoundExclusive();
        }

        
BigDecimal roundedTax = tax.setScale(MONEY_SCALE, RoundingMode.HALF_UP);

BigDecimal basicPersonalAmount = calculateBasicPersonalAmount(taxableIncome);

BigDecimal potentialCredit = basicPersonalAmount
            .multiply(new BigDecimal("0.14"))
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        // A non-refundable credit cannot reduce federal tax below zero.
        BigDecimal creditApplied = potentialCredit.min(roundedTax);

        BigDecimal taxAfterCredits = roundedTax.subtract(creditApplied)
            .max(BigDecimal.ZERO)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new TaxResult(
            grossIncome.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
            deductions.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
            taxableIncome.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
            roundedTax,
            basicPersonalAmount,
            creditApplied,
            taxAfterCredits,
            lines);

    }


    private static BigDecimal calculateBasicPersonalAmount(BigDecimal taxableIncome) {
        BigDecimal maximum = new BigDecimal("16452.00");
        BigDecimal minimum = new BigDecimal("14829.00");
        BigDecimal phaseOutStarts = new BigDecimal("181440.00");
        BigDecimal phaseOutEnds = new BigDecimal("258482.00");

        if (taxableIncome.compareTo(phaseOutStarts) <= 0) {
            return maximum;
        }

        if (taxableIncome.compareTo(phaseOutEnds) >= 0) {
            return minimum;
        }

        BigDecimal phaseOutRange = phaseOutEnds.subtract(phaseOutStarts);
        BigDecimal incomeAboveThreshold = taxableIncome.subtract(phaseOutStarts);
        BigDecimal reductionRange = maximum.subtract(minimum);

        BigDecimal reduction = reductionRange
            .multiply(incomeAboveThreshold)
            .divide(phaseOutRange, 10, RoundingMode.HALF_UP);

        return maximum.subtract(reduction)
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }


    private static void requireNonNegative(BigDecimal value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(label + " cannot be negative");
        }
    }
}
