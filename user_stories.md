# SKU Profitability — User Stories

Repository: 251027-Java/P2_SKUsProfitability 

---

## Overview

This document contains user stories for the SKU Profitability project.

Goal: reliably bring SKU, transaction, cost and inventory data into the system.

1. Story: Fetch product master data from internal REST API
   - As a Data Engineer, I want the system to fetch product master data (SKU metadata and cost) from an internal REST API so that product attributes and costs are up to date.

2. Story: Validate incoming data
   - As a Developer, I want the system to validate incoming data for required fields and types so downstream calculations are reliable.

3. Story: Normalized database schema
   - As a Backend Engineer, I want a normalized database schema for SKUs, transactions, costs, and inventory snapshots so queries and joins are efficient.

4. Story: Time-series inventory snapshots
   - As an Analyst, I want the system to maintain time-series snapshots of inventory and cost so profitability over time can be computed accurately.

5. Story: Compute gross profit per SKU
   - As a Product Manager, I want the system to compute gross profit per SKU (revenue - cost_of_goods_sold) so I can see which SKUs are profitable.

6. Story: Apply overhead allocation rules
   - As a Finance Analyst, I want the ability to apply per-SKU or per-category overhead allocation rules so net profitability reflects indirect costs.

7. Story: Incremental profit recalculation
   - As an Engineer, I want profit recalculation to be incremental so recalculation after a data correction is efficient.

8. Story: Top SKUs dashboard
    - As an Analyst, I want a dashboard that shows top 20 SKUs by gross profit over a selectable period so I can focus on high-impact items.

9. Story: Profitability trend chart for SKU
    - As a Product Manager, I want a profitability trend chart for any SKU (revenue, cost, gross profit) to detect lifecycle changes.

10. Story: Filter & export SKUs by profitability thresholds
    - As an Operations user, I want to filter SKUs by profitability thresholds (e.g., margin < 10%) and export the list so I can take remediation actions.

11. Story: Show provenance of profitability numbers
    - As a Designer, I want the UI to show provenance of profitability numbers (which input records and rule produced the result) so users trust the data.

12. Story: Alert on margin drop
    - As a Category Manager, I want automated alerts when an SKU's margin drops below a threshold so I can investigate and respond quickly.

13. Story: Detect rapid sales velocity changes
    - As a Sales Manager, I want the system to detect rapid sales velocity changes (spike/drop) for SKUs so we can adjust stock or pricing.

14. Story: REST API for profitability results
    - As an Integration Engineer, I want a REST API endpoint to return profitability results for a SKU and period so external dashboards and automations can consume them.

15. Story: Scheduled exports to S3/FTP
    - As a Business User, I want scheduled exports (S3 or FTP) of profitability reports so I can load them to other systems.

16. Story: Role-based access control (RBAC)
    - As an Admin, I want role-based access control (RBAC) so only authorized users can view or modify sensitive cost data.

17. Story: Audit logging for data changes
    - As a Compliance Officer, I want audit logging for data imports, edits, and recalculations so we can trace changes for audits.

18. Story: Tests for profitability calculations
    - As a QA Engineer, I want unit and integration tests for the profitability calculations so results are correct across edge cases.

19. Story: Application and job monitoring
    - As a DevOps Engineer, I want application and job monitoring (metrics + alerts) so we detect import failures and slow queries quickly.

20. Story: Scalable batch calculation jobs
    - As a Platform Engineer, I want batch calculation jobs to be schedulable and horizontally scalable so processing time remains bounded as data grows.

21. Story: Pre-aggregated summary tables
    - As a Data Analyst, I want pre-aggregated summary tables (materialized views) for common queries so dashboard performance is fast.

22. Story: Multi-currency support
    - As a User, I want multi-currency support with configurable exchange rates so profitability can be reported in a chosen base currency.

23. Story: Scenario simulation for pricing/cost
    - As a Business Analyst, I want scenario simulation (change price/cost and see impact) so we can test pricing decisions without altering real data.
    
---