$sku = "TEST-SKU-" + (Get-Random)
$url = "http://localhost:8083/api/skus"

# 1. Create SKU
$body = @{
    sku = $sku
    productName = "Test Product"
    description = "Test Description"
    length = 10
    width = 10
    height = 10
    weight = 5
    category = "Test Category"
    sellingPrice = 100.00
} | ConvertTo-Json

Write-Host "Creating SKU: $sku"
Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"

# 2. Trigger Kafka Update
$triggerUrl = "http://localhost:8083/api/skus/test-kafka?sku=$sku"
Write-Host "Triggering Kafka Update..."
Invoke-RestMethod -Uri $triggerUrl -Method Post

# 3. Wait and Check Logs
Start-Sleep -Seconds 5
Write-Host "Checking Calculator Logs..."
docker-compose logs calculator-service | Select-String $sku
