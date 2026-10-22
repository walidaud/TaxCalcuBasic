
package dev.walid.tax;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class TaxCalcuBasicGUI extends JFrame {

    private static final NumberFormat CAD =
            NumberFormat.getCurrencyInstance(Locale.CANADA);

    private final JTextField incomeField = new JTextField("60000", 16);
    private final JTextField deductionsField = new JTextField("0", 16);
    private final JTextArea resultsArea = new JTextArea();

    private final FederalTaxCalculator calculator =
            new FederalTaxCalculator(TaxConfig.federalBrackets2026());

    public TaxCalcuBasicGUI() {
        super("TaxCalcuBasic | 2026 Federal Tax Estimator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(580, 650));

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(24, 28, 24, 28));
        root.setBackground(new Color(246, 248, 251));
        setContentPane(root);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("TaxCalcuBasic");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(new Color(30, 50, 80));

        JLabel subtitle =
                new JLabel("2026 Canadian federal income tax estimator");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(90, 100, 115));

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        // Input form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 233)),
                new EmptyBorder(18, 18, 18, 18)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(form, gbc, 0, "Gross annual income (CAD)", incomeField);
        addFormRow(form, gbc, 1, "Eligible deductions (CAD)", deductionsField);

        JButton calculateButton = new JButton("Calculate Tax");
        calculateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        calculateButton.setBackground(new Color(35, 95, 175));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton clearButton = new JButton("Clear");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(calculateButton);
        buttons.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 4, 4, 4);
        form.add(buttons, gbc);

        // Results
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultsArea.setLineWrap(false);
        resultsArea.setMargin(new Insets(12, 12, 12, 12));
        resultsArea.setText(
                "Enter your annual income and eligible deductions.\n\n"
                + "Select Calculate Tax to view your estimate."
        );

        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        resultsScroll.setPreferredSize(new Dimension(500, 340));
        resultsScroll.setBorder(
                BorderFactory.createTitledBorder("Tax Estimate"));

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(resultsScroll, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        JLabel disclaimer = new JLabel(
                "<html><div style='width:490px'>"
                + "Educational estimate using configured 2026 federal "
                + "brackets and a simplified basic personal amount credit. "
                + "Does not calculate provincial or territorial tax, CPP, "
                + "EI, or all CRA credits and adjustments. Not tax advice."
                + "</div></html>"
        );
        disclaimer.setForeground(new Color(100, 100, 110));
        disclaimer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        root.add(disclaimer, BorderLayout.SOUTH);

        calculateButton.addActionListener(e -> calculateTax());
        clearButton.addActionListener(e -> clearForm());

        getRootPane().setDefaultButton(calculateButton);

        pack();
        setMinimumSize(new Dimension(580, 650));
        setLocationRelativeTo(null);
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JTextField field) {

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.45;
        gbc.insets = new Insets(8, 4, 8, 12);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        gbc.insets = new Insets(8, 4, 8, 4);

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(field, gbc);
    }

    private void calculateTax() {
        try {
            BigDecimal income =
                    parseAmount(incomeField.getText(), "Gross income");

            BigDecimal deductions =
                    parseAmount(deductionsField.getText(), "Deductions");

            TaxResult result = calculator.calculate(income, deductions);

            BigDecimal taxBeforeCredits =
                    result.estimatedFederalTaxBeforeCredits();

            BigDecimal personalAmount =
                    result.basicPersonalAmount();

            BigDecimal creditApplied =
                    result.basicPersonalAmountCreditApplied();

            BigDecimal taxAfterCredits =
                    result.estimatedFederalTaxAfterCredits();

            BigDecimal taxableIncome = result.taxableIncome();

            BigDecimal effectiveRate = BigDecimal.ZERO;

            if (taxableIncome.signum() > 0) {
                effectiveRate = taxAfterCredits
                        .multiply(new BigDecimal("100"))
                        .divide(taxableIncome, 2, RoundingMode.HALF_UP);
            }

            BigDecimal incomeAfterDeductions =
                    result.grossIncome().subtract(result.deductions());

            BigDecimal remainingIncome =
                    incomeAfterDeductions.subtract(taxAfterCredits);

            StringBuilder output = new StringBuilder();

            output.append("2026 FEDERAL TAX ESTIMATE\n");
            output.append("========================================\n");

            appendMoney(output, "Gross income:",
                    result.grossIncome());

            appendMoney(output, "Eligible deductions:",
                    result.deductions());

            appendMoney(output, "Taxable income:",
                    taxableIncome);

            output.append("\nMARGINAL BRACKET BREAKDOWN\n");
            output.append("----------------------------------------\n");

            if (result.breakdown().isEmpty()) {
                output.append("No taxable income in the configured brackets.\n");
            } else {
                for (TaxResult.BracketLine line : result.breakdown()) {
                    String rate = line.rate()
                            .multiply(new BigDecimal("100"))
                            .stripTrailingZeros()
                            .toPlainString() + "%";

                    output.append(String.format(
                            Locale.CANADA,
                            "%s at %s -> %s%n",
                            CAD.format(line.incomeInBracket()),
                            rate,
                            CAD.format(line.taxInBracket())
                    ));
                }
            }

            output.append("\nFEDERAL TAX SUMMARY\n");
            output.append("----------------------------------------\n");

            appendMoney(output, "Tax before credits:",
                    taxBeforeCredits);

            appendMoney(output, "Basic personal amount:",
                    personalAmount);

            appendMoney(output, "Credit applied:",
                    creditApplied);

            appendMoney(output, "Tax after credit:",
                    taxAfterCredits);

            output.append(String.format(
                    Locale.CANADA,
                    "%-29s %13.2f%%%n",
                    "Effective federal tax rate:",
                    effectiveRate
            ));

            output.append("\nINCOME SUMMARY\n");
            output.append("----------------------------------------\n");

            appendMoney(output, "Income after deductions:",
                    incomeAfterDeductions);

            appendMoney(output, "After estimated federal tax:",
                    remainingIncome);

            output.append("\nNOTES\n");
            output.append("----------------------------------------\n");
            output.append("Tax after credit is federal tax only.\n");
            output.append("Provincial tax, CPP and EI are excluded.\n");
            output.append("The estimate is not an official CRA result.\n");

            resultsArea.setText(output.toString());
            resultsArea.setCaretPosition(0);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage() != null
                            ? ex.getMessage()
                            : "Please enter valid amounts.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void appendMoney(
            StringBuilder output,
            String label,
            BigDecimal amount) {

        output.append(String.format(
                Locale.CANADA,
                "%-29s %14s%n",
                label,
                CAD.format(amount)
        ));
    }

    private BigDecimal parseAmount(String input, String fieldName) {
        String cleaned = input.trim().replace(",", "");

        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " is required.");
        }

        try {
            BigDecimal amount = new BigDecimal(cleaned);

            if (amount.signum() < 0) {
                throw new IllegalArgumentException(
                        fieldName + " cannot be negative.");
            }

            return amount;

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid number, such as 60000.00.");
        }
    }

    private void clearForm() {
        incomeField.setText("");
        deductionsField.setText("0");
        resultsArea.setText(
                "Enter your annual income and eligible deductions.\n\n"
                + "Select Calculate Tax to view your estimate."
        );
        incomeField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use Swing's default appearance if unavailable.
            }

            new TaxCalcuBasicGUI().setVisible(true);
        });
    }
}
