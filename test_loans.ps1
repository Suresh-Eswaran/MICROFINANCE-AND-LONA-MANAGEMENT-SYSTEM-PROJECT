$officerJson = @{
    name = "Loan Officer"
    email = "loanofficer3@example.com"
    password = "password123"
    role = "ROLE_OFFICER"
} | ConvertTo-Json

$registerResp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $officerJson -ContentType "application/json"
$officerToken = $registerResp.token

$managerJson = @{
    name = "Branch Manager"
    email = "branchmanager3@example.com"
    password = "password123"
    role = "ROLE_MANAGER"
} | ConvertTo-Json

$registerResp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $managerJson -ContentType "application/json"
$managerToken = $registerResp.token

$loanPayload = @{
    clientId = 1
    productId = 1
    amountRequested = 10000
    purpose = "Test Loan"
    officerId = 1
} | ConvertTo-Json

$headersOfficer = @{ Authorization = "Bearer $officerToken" }
$headersManager = @{ Authorization = "Bearer $managerToken" }

Write-Host "--- 1. POST /api/loans/apply ---"
try {
    $applyResp = Invoke-RestMethod -Uri "http://localhost:8080/api/loans/apply" -Method Post -Headers $headersOfficer -Body $loanPayload -ContentType "application/json" -ErrorAction Stop
    $loanId = $applyResp.id
    $applyResp | ConvertTo-Json
} catch {
    Write-Host "Error in POST /api/loans/apply: $_"
    $loanId = 1 # fallback if error
}

Write-Host "--- 2. GET /api/loans/$loanId ---"
try {
    $getLoanResp = Invoke-RestMethod -Uri "http://localhost:8080/api/loans/$loanId" -Method Get -Headers $headersOfficer
    $getLoanResp | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Error in GET /api/loans/$loanId: $_"
}

Write-Host "--- 3. PUT /api/loans/$loanId/approve ---"
try {
    $approveResp = Invoke-RestMethod -Uri "http://localhost:8080/api/loans/$loanId/approve" -Method Put -Headers $headersManager
    $approveResp | ConvertTo-Json
} catch {
    Write-Host "Error in PUT /api/loans/$loanId/approve: $_"
}

Write-Host "--- 4. POST /api/loans/$loanId/disburse ---"
try {
    $disburseResp = Invoke-RestMethod -Uri "http://localhost:8080/api/loans/$loanId/disburse?channel=NEFT" -Method Post -Headers $headersManager
    $disburseResp | ConvertTo-Json
} catch {
    Write-Host "Error in POST /api/loans/$loanId/disburse: $_"
}

Write-Host "--- 5. GET /api/loans/client/1 ---"
try {
    $clientLoans = Invoke-RestMethod -Uri "http://localhost:8080/api/loans/client/1" -Method Get -Headers $headersOfficer
    $clientLoans | ConvertTo-Json
} catch {
    Write-Host "Error in GET /api/loans/client/1: $_"
}
