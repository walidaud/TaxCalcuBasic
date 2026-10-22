
package dev.walid.tax;

import java.math.BigDecimal;
import java.util.List;

/**
 * Federal tax brackets for the 2026 tax year.
 * Rates are expressed as decimals.
 *
 * Source: Canada Revenue Agency, 2026 tax rates and brackets.
 */
public final class TaxConfig {
    private TaxConfig() {}

    public static List<TaxBracket> federalBrackets2026() {
        return List.of(
                new TaxBracket(new BigDecimal("58523.00"), new BigDecimal("0.14")),
                new TaxBracket(new BigDecimal("117045.00"), new BigDecimal("0.205")),
                new TaxBracket(new BigDecimal("181440.00"), new BigDecimal("0.26")),
                new TaxBracket(new BigDecimal("258482.00"), new BigDecimal("0.29")),
                new TaxBracket(null, new BigDecimal("0.33"))
        );
    }

    /**
     * Retained so existing code using the old method still compiles.
     */
    public static List<TaxBracket> demonstrationBrackets() {
        return federalBrackets2026();
    }
}
