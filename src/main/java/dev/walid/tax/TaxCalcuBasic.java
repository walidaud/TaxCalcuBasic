package dev.walid.tax;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

public final class TaxCalcuBasic {
    private static final NumberFormat CAD =
            NumberFormat.getCurrencyInstance(Locale.CANADA);

    private TaxCalcuBasic() {}

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("TaxCalcuBasic — simplified federal tax estimator");
            System.out.println("Demo brackets only; excludes credits and provincial tax.");
            BigDecimal income = readAmount(scanner, "Gross annual income (CAD): ");
            BigDecimal deductions = readAmount(scanner, "Eligible deductions (CAD): ");

            FederalTaxCalculator calculator =
                    new FederalTaxCalculator(TaxConfig.demonstrationBrackets());
            TaxResult result = calculator.calculate(income, deductions);

            System.out.println("\\n--- Estimate ---");
            System.out.println("Gross income:       " + CAD.format(result.grossIncome()));
            System.out.println("Deductions:         " + CAD.format(result.deductions()));
            System.out.println("Taxable income:     " + CAD.format(result.taxableIncome()));
            System.out.println("\\nBracket breakdown:");
            for (TaxResult.BracketLine line : result.breakdown()) {
                System.out.printf("  %s at %s%% -> %s%n",
                        CAD.format(line.incomeInBracket()),
                        line.rate().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString(),
                        CAD.format(line.taxInBracket()));
            }
            System.out.println("\\nEstimated federal tax before credits: "
                    + CAD.format(result.estimatedFederalTaxBeforeCredits()));
            System.out.println("This is an educational estimate, not tax advice.");
        } catch (IllegalArgumentException ex) {
            System.err.println("Input error: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static BigDecimal readAmount(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(raw);
            if (amount.signum() < 0) {
                throw new IllegalArgumentException("amount cannot be negative");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("enter a valid number such as 65000.00");
        }
    }
}
