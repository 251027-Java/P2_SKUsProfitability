# Quick Start Guide - Microservice Testing

## Prerequisites
- ✓ PostgreSQL database running (docker start sku-profitability-db)
- ✓ Both services compiled and ready

## Step-by-Step Testing

### 1. Start the Database (if not running)
```powershell
docker start sku-profitability-db
```

### 2. Start CalculatorService (Terminal 1)
```powershell
cd C:\Users\testing\RevatureTraining\P2_SKUsProfitability\backend\CalculatorService
.\mvnw.cmd spring-boot:run
```
Wait for: `Started CalculatorServiceApplication in X.XXX seconds`

### 3. Start Main Backend (Terminal 2)
```powershell
cd C:\Users\testing\RevatureTraining\P2_SKUsProfitability\backend\SKUProfitability
.\mvnw.cmd spring-boot:run
```
Wait for: `Started SKUProfitabilityApplication in X.XXX seconds`

### 4. Run Integration Test
```powershell
# In a new terminal (Terminal 3)
cd C:\Users\testing\RevatureTraining\P2_SKUsProfitability
.\test-microservice-integration.ps1
```

## Expected Output

```
========================================
 TESTING MICROSERVICE INTEGRATION
========================================

Step 1: Checking CalculatorService...
✓ CalculatorService Status: UP

Step 2: Testing Main Backend → CalculatorService communication...
✓ Main Backend successfully called CalculatorService!

  CALCULATION RESULTS:
  ------------------
  Selling Price: $59.99
  FBA Fee: $6.74
  Referral Fee: $9.00
  Total Fees (Jan-Sep): $19.97
  Total Fees (Oct-Dec): $25.97
  Net Profit (Jan-Sep): $40.02
  Net Profit (Oct-Dec): $34.02
  Size Tier: Large Standard

Step 3: Verifying calculation was saved to database...
✓ Total calculations in database: 2

========================================
 MICROSERVICE INTEGRATION SUCCESSFUL!
========================================

✓ Main Backend (8080) → CalculatorService (8081) → Database
✓ Calculation history stored in 'calculations' table
```

## Testing Individual Services

### Test CalculatorService Directly
```powershell
curl http://localhost:8081/api/calculator/health
```

### Test Main Backend Health
```powershell
curl http://localhost:8080/api/calculator/health
```

### View Calculation History in Database
```powershell
docker exec sku-profitability-db psql -U postgres -d sku_profitability_db -c "SELECT id, user_id, selling_price, net_profit_jan_sep, created_at FROM calculations ORDER BY created_at DESC LIMIT 5;"
```

## Troubleshooting

### CalculatorService won't start
- Check if port 8081 is already in use: `netstat -ano | findstr :8081`
- Verify database is running: `docker ps | findstr sku-profitability-db`

### Main Backend can't reach CalculatorService
- Ensure CalculatorService started successfully
- Check the URL in application.properties: `calculator.service.url=http://localhost:8081`
- Look for errors in Main Backend console

### "Service Unavailable" error
- CalculatorService may have crashed
- Check CalculatorService terminal for errors
- Restart CalculatorService

## Docker Compose Alternative

Instead of running in separate terminals, use Docker Compose:

```powershell
docker-compose up --build
```

This will start all services together. Access:
- Main Backend: http://localhost:8080
- CalculatorService: http://localhost:8081
- Database: localhost:5432

Stop all services:
```powershell
docker-compose down
```

## Success Indicators

✅ Both services show "Started" message
✅ Health endpoints return {"status":"UP"}
✅ Test script shows all green checkmarks
✅ Database contains calculation records
✅ Main Backend health shows: "calculatorServiceReachable": true
