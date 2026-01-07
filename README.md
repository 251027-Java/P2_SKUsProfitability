# SKU Profitability Calculator

A web application for Amazon sellers to analyze product profitability by calculating FBA fees, storage costs, and determining optimal source pricing for products.

## Overview

This application helps Amazon sellers make informed decisions about which products to sell by:
- Collecting data from BrightData API
- Calculating Amazon FBA fees based on product dimensions and weight
- Determining storage fees for different time periods
- Analyzing profitability metrics (net profit, margin)
- Organizing products into lists for better management

## Features

### Product Management
- **API SKU Management**: API gets SKUs for all products
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

1. **API Product Management**: API gets SKUs for all products and populates the SKU table and product-service 

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

P2 Requirements: 

This project will be a full stack application (db, API, SPA ui), in MSA, deployed to the AWS cloud
The api/backend should consist of more than one service.
Your presentation sould include a demonstration of the running application
Your presentation shoudl be between 15-25 minutes, at 35 you're getting cut off!
Your project must fulfill all requirements from P1:
SPA must...

Must include routing to at least two separate pages/views.
Must include HTTP requests to your API.
Must include at least 5 different components.
Must demonstrate two-way binding.
Your backend must expose a RESTful API.

Your backend must use Spring Data JPA for database interactions.
Your backend must demonstate separating concerns through application layers.
Your backend must feature at least two custom classes.
Your backend must feature verbose exception handling to prevent unexpected crashing.
Your backend API must include at least 50% overall line coverage with Junit testing (use Mockito to test the serivce layer).
Your backend API must include some flavor of Authentication and Authorization.
Your database must be a PostgreSQL instance running in a Docker container.

Must be in 3NF.
Must have at least 5 tables.
Must have at least 1 Many to Many relationship.
Your project must be kept in a Git repository that is part of the cohort Organization.

Your repo should demonstrate best practices with branching (no commits directly to main!)
Your repo should include a README.md that details how to start and run your full stack app.
You should include a Description file
Describe the app
include the user stories that your project fulfills (as a ____ I want to_____ so that I can____ )
include a wireframe for the frontend
include the ERD for your database
include endpoint documentation for your API
Your Team...

Should implement some kind of project board to track work and progress over the duration of the project
Should hold a regular stand up meeting (even when working remotely!) to keep team members in sync.
Should have a represetative (they do need to be a team "lead", but they can) to report on the project progress.
Your project must be containerized (with docker)

Service images should be pushed to Docker Hub

Your project should be able to start with a docker compose for local development

Your project should deploy to a Kubernetes cluster running in AWS

Your project should be tested and deployed automatically with a PR or push to your repository (through Jenkins)

Your application should include a Eureka Server

Your application should include a Kafka instance

Your application should either manage traffic through K8s or a Spring Gateway

Your application should include log generation across all levels when appropriate