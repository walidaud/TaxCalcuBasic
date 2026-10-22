package dev.walid.tax;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One marginal federal tax bracket. upperBoundExclusive is null for the top bracket.
 */
public record TaxBracket(BigDecimal upperBoundExclusive, BigDecimal rate) {
    public TaxBracket {
        Objects.requireNonNull(rate, "rate must not be null");
        if (upperBoundExclusive != null && upperBoundExclusive.signum() <= 0) {
            throw new IllegalArgumentException("Bracket upper bound must be positive");
        }
        if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1");
        }
    }
}
