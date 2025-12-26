# SKU Profitability Calculator

A web application for Amazon sellers to analyze product profitability by calculating FBA fees, storage costs, and determining optimal source pricing for products.

## Overview

This application helps Amazon sellers make informed decisions about which products to sell by:
- Importing product data via CSV files
- Calculating Amazon FBA fees based on product dimensions and weight
- Determining storage fees for different time periods
- Analyzing profitability metrics (net profit, margin)
- Organizing products into lists for better management

## Features

### Product Management
- **CSV Import**: Bulk import products with dimensions, weight, pricing, and category data
- **Manual Entry**: Create individual SKUs with all product details
- **Search**: Find products quickly by SKU
- **Lists**: Organize products into custom lists for portfolio management

### Profitability Calculator
- **FBA Fee Calculation**: Automatically calculates fulfillment fees based on size tier and weight
- **Storage Fees**: Calculates storage costs for Jan-Sep ($0.75/cubic foot) and Oct-Dec ($1.92/cubic foot) periods
- **Referral Fees**: Category-based referral fee calculation (default 15%, customizable)
- **Additional Costs**: Support for freight costs and other expenses
- **Profitability Metrics**: 
  - Net profit (for both storage periods)
  - Profit margin percentage

### Dashboard
- **List-Centric View**: Filter metrics by specific lists or view all products
- **Key Metrics**:
  - Average Amazon price
  - Average total fees
  - Products needing attention (negative profit)
  - Category distribution
  - Size classification breakdown
- **Action Items**: 
  - Products not in any list
  - Products with negative profit potential
- **Lists Overview**: Summary of all lists with item counts and metrics

## CSV Import Format

When importing products via CSV, include the following columns:

**Required Fields (must have values):**
- **SKU**: Product SKU identifier (must be unique, alphanumeric with underscores/hyphens, max 50 characters)
- **Product Name**: Name of the product (max 200 characters)
- **Description**: Product description (text field)
- **Category**: Product category (affects referral fees, max 50 characters)

**Optional Fields (can be empty):**
- **Length**: Product length in inches (numeric)
- **Width**: Product width in inches (numeric)
- **Height**: Product height in inches (numeric)
- **Weight**: Product weight in pounds (numeric)
- **Selling Price**: Average Amazon price over 90 days (numeric, can include $ and commas)

**Important Notes:**
- **Header names are case-insensitive** (e.g., "SKU", "sku", "Sku" all work)
- **Numeric fields** can include currency symbols ($) and commas, which are automatically removed during parsing
- **Dimensions and weight** are optional but highly recommended for accurate fee calculations
- Without dimensions and weight, the application cannot calculate:
  - Size classification (Small Standard, Large Standard, Small Oversize, Large Oversize)
  - FBA fulfillment fees
  - Storage fees
- Without selling price, the application cannot calculate:
  - Referral fees
  - Net profit
  - Profit margin

The application automatically:
- Calculates size classification (Small Standard, Large Standard, Small Oversize, Large Oversize)
- Determines FBA fulfillment fees
- Calculates **Amazon's 15% referral fee** (default, minimum $0.30) based on category
- Computes profitability metrics

**Important**: The 15% Amazon referral fee is automatically included in all fee calculations. This represents Amazon's commission on every sale and is a standard fee across most product categories.

## Product Size Classifications

Products are automatically classified into size tiers based on dimensions and weight:

- **Small Standard**: ≤15" x 12" x 0.75", ≤0.75 lbs
- **Large Standard**: ≤18" x 14" x 8", ≤20 lbs
- **Small Oversize**: ≤60" x 30" x 30", ≤70 lbs
- **Large Oversize**: Everything else

Each size tier has different FBA fulfillment fee structures.

## Fee Calculation Details

### FBA Fulfillment Fee
Based on size tier and billable weight (max of actual weight or dimensional weight):
- Dimensional weight = (L × W × H) / 166
- Billable weight = max(actual weight, dimensional weight)
- Fee varies by size tier with base fee + weight-based charges

### Storage Fee
Calculated per cubic foot per month:
- **Jan-Sep**: $0.75 per cubic foot per month
- **Oct-Dec**: $1.92 per cubic foot per month

### Referral Fee (Amazon's Commission)
**Amazon charges a 15% referral fee** on all product sales. This fee is automatically included in all calculations:
- **Default Rate**: 15% of the selling price
- **Minimum Fee**: $0.30 per item (even if 15% of price is less)
- **Calculation**: Referral Fee = max(Selling Price × 15%, $0.30)
- **Customizable**: In the calculator, you can adjust the referral fee percentage if your category has a different rate

**Example**: For a product selling at $20.00:
- Referral Fee = $20.00 × 15% = $3.00

This 15% Amazon referral fee is automatically included in the total fees calculation for all SKUs and profitability calculations.

## How It Works

1. **Import Products**: Upload a CSV file with product data or manually create SKUs
2. **Automatic Calculations**: The system automatically calculates:
   - Size classification based on dimensions
   - FBA fulfillment fees
   - **Amazon's 15% referral fee** (included in all calculations)
   - Storage fees for both time periods
   - Net profit and margin
   
   **Total Fees** = FBA Fulfillment Fee + **15% Referral Fee** + Storage Fee + Freight Costs + Other Costs
3. **Profitability Analysis**: Use the calculator to:
   - Enter product dimensions and pricing
   - Adjust time in storage, freight costs, and other expenses
   - View profitability metrics for both storage fee periods
4. **List Management**: Organize products into lists to:
   - Group related products
   - View list-specific metrics on the dashboard
   - Track portfolio performance
5. **Dashboard Insights**: Monitor:
   - Average pricing and fees across products
   - Products requiring attention (negative profit)
   - Category and size distribution
   - Unlisted products
