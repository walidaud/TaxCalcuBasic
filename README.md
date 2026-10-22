# TaxCalcuBasic

A small Java command-line project demonstrating a configurable marginal tax calculator, unit testing, Docker packaging, and continuous integration.

## Features

- Java 21 and Maven
- Configurable marginal tax brackets
- Gross income and deductions as inputs
- Bracket-by-bracket estimate
- `BigDecimal` for monetary calculations
- JUnit 5 tests for bracket math, boundaries, deductions, invalid input, and configuration
- Docker image and GitHub Actions CI

## Important scope note

This is an educational example, **not a complete Canadian tax calculator**. It currently calculates tax from the configured marginal brackets only. It does not calculate provincial or territorial tax, non-refundable credits, payroll contributions, benefit clawbacks, special income rules, or an official tax return. The included bracket configuration is demonstration data and must be checked against the CRA's official information before being used for a particular tax year.

Official starting point: https://www.canada.ca/en/revenue-agency/services/tax/individuals.html

## Requirements

- JDK 21
- Maven 3.9+ (or use Docker)
- VS Code with the Extension Pack for Java

## Run in VS Code / terminal

Run tests first:

```bash
mvn clean verify
```

Start the calculator:

```bash
mvn -q -DskipTests package
java -cp target/classes dev.walid.tax.TaxCalcuBasic
```

Example input:

```text
Gross annual income (CAD): 65000
Eligible deductions (CAD): 3000
```

The result is an estimate before credits and should not be interpreted as final tax payable.

## Run with Docker

Build the image:

```bash
docker build -t taxcalcu-basic .
```

Run interactively:

```bash
docker run --rm -it taxcalcu-basic
```

Or use Compose:

```bash
docker compose run --rm taxcalcu-basic
```

## CI

GitHub Actions runs `mvn clean verify` for pushes, pull requests, and manual dispatches. The workflow checks out the repository, configures Temurin Java 21, caches Maven dependencies, and executes the test suite.

## Project structure

```text
src/
  main/java/dev/walid/tax/
    FederalTaxCalculator.java
    Main.java
    TaxBracket.java
    TaxConfig.java
    TaxResult.java
  test/java/dev/walid/tax/
    FederalTaxCalculatorTest.java
.github/workflows/ci.yml
Dockerfile
compose.yaml
pom.xml
```

## Good next improvements

1. Add a clearly versioned, source-linked tax configuration for a selected tax year.
2. Add provincial/territorial tax as a separate calculator.
3. Model credits separately from gross bracket tax.
4. Add parameterized tests for every bracket boundary.
5. Add a JSON output mode for easier integration testing.
