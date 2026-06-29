$Base = "http://localhost:8080"
$login = '{"phoneOrEmail":"9876500001","password":"password1"}'
try {
    $r = Invoke-RestMethod -Uri "$Base/api/auth/login" -Method POST -ContentType "application/json" -Body $login
    $r | ConvertTo-Json -Depth 5
} catch {
    Write-Host "ERR" $_.Exception.Message
    if ($_.ErrorDetails.Message) { Write-Host $_.ErrorDetails.Message }
}
