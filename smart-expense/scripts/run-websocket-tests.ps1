$ErrorActionPreference = "Stop"
$Base = "http://localhost:8080"
$Failed = 0

function Write-TestResult($num, $name, $pass, $detail) {
    $status = if ($pass) { "PASS" } else { "FAIL" }
    Write-Host ("[{0}] TEST {1}: {2} - {3}" -f $status, $num, $name, $detail)
    if (-not $pass) { $script:Failed++ }
}

function Invoke-Api {
    param([string]$Method, [string]$Path, [string]$Token, $Body = $null)
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $params = @{ Uri = "$Base$Path"; Method = $Method; Headers = $headers; ErrorAction = "Stop" }
    if ($null -ne $Body) { $params["Body"] = ($Body | ConvertTo-Json -Depth 10 -Compress) }
    try {
        $data = Invoke-RestMethod @params
        return [PSCustomObject]@{ Json = $data; IsError = $false; ApiError = $null }
    } catch {
        $apiError = $_.Exception.Message
        if ($_.Exception.Response) {
            try {
                $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
                $body = $reader.ReadToEnd()
                $reader.Close()
                $parsed = $body | ConvertFrom-Json
                if ($parsed.error) { $apiError = [string]$parsed.error }
            } catch { }
        }
        return [PSCustomObject]@{ Json = $null; IsError = $true; ApiError = $apiError }
    }
}

function Login($phone, $password) {
    $r = Invoke-Api -Method POST -Path "/api/auth/login" -Body @{ phoneOrEmail = $phone; password = $password }
    if ($r.IsError) { throw "Login failed for $phone : $($r.ApiError)" }
    return $r.Json.data
}

try {
    Invoke-RestMethod -Uri "$Base/api/health" -Method GET | Out-Null
} catch {
    Write-Host "ERROR: Start Spring Boot on port 8080 and MySQL first."
    exit 1
}

$pwd = "password1"
$Riya = Login "9876500001" $pwd
$Arjun = Login "9876500002" $pwd

# Find or create ACTIVE trip
$trips = (Invoke-Api -Method GET -Path "/api/trips" -Token $Riya.accessToken).Json.data
$trip = $trips | Where-Object { $_.status -eq "ACTIVE" } | Select-Object -First 1
if (-not $trip) {
    $tripId = (Invoke-Api -Method POST -Path "/api/trips" -Token $Riya.accessToken -Body @{
        name = "WS Test Trip"; kittyTarget = 0
    }).Json.data.tripId
    Invoke-Api -Method POST -Path "/api/trips/$tripId/invite" -Token $Riya.accessToken -Body @{ phone = "9876500002"; role = "MEMBER" } | Out-Null
    Invoke-Api -Method PATCH -Path "/api/trips/$tripId/status" -Token $Riya.accessToken -Body @{ newStatus = "ACTIVE" } | Out-Null
    $trip = (Invoke-Api -Method GET -Path "/api/trips/$tripId" -Token $Riya.accessToken).Json.data
} else {
    $tripId = $trip.tripId
}

Write-Host "Using trip: $tripId (status=$($trip.status))`n"

# PART C - trigger events (broadcasts verified via server logs / manual browser)
$r1 = Invoke-Api -Method POST -Path "/api/trips/$tripId/kitty/deposit" -Token $Riya.accessToken -Body @{ amount = 100; method = "MANUAL" }
Write-TestResult "C1" "Kitty deposit broadcast API" (-not $r1.IsError) $(if ($r1.IsError) { $r1.ApiError } else { "deposit OK" })

$r2 = Invoke-Api -Method POST -Path "/api/trips/$tripId/expenses" -Token $Riya.accessToken -Body @{
    title = "WS Test Expense"; amount = 200; equalSplit = $true
    participants = @(@{ userId = $Riya.userId }, @{ userId = $Arjun.userId })
}
Write-TestResult "C2" "Expense added broadcast API" (-not $r2.IsError) $(if ($r2.IsError) { $r2.ApiError } else { "expense OK" })

if ($trip.status -eq "ACTIVE") {
    $r3 = Invoke-Api -Method PATCH -Path "/api/trips/$tripId/status" -Token $Riya.accessToken -Body @{ newStatus = "FROZEN" }
    Write-TestResult "C3" "Status change broadcast API" (-not $r3.IsError) $(if ($r3.IsError) { $r3.ApiError } else { "FROZEN" })
}

$r5 = Invoke-Api -Method POST -Path "/api/ws-test/broadcast/$tripId" -Token $Riya.accessToken -Body "hello from test"
Write-TestResult "C5" "Test broadcast endpoint" (-not $r5.IsError) $(if ($r5.IsError) { $r5.ApiError } else { $r5.Json.data })

# PART D - regression
$d1 = Invoke-Api -Method GET -Path "/api/auth/me" -Token $Riya.accessToken
Write-TestResult "D1" "GET /api/auth/me" (-not $d1.IsError) "200"

$d2 = Invoke-Api -Method GET -Path "/api/trips" -Token $Riya.accessToken
Write-TestResult "D2" "GET /api/trips" (-not $d2.IsError) "200"

$d3 = Invoke-Api -Method GET -Path "/api/trips/$tripId/kitty" -Token $Riya.accessToken
Write-TestResult "D3" "GET kitty" (-not $d3.IsError) "200"

$d4 = Invoke-Api -Method GET -Path "/api/trips/$tripId/expenses" -Token $Riya.accessToken
Write-TestResult "D4" "GET expenses" (-not $d4.IsError) "200"

$settled = $trips | Where-Object { $_.status -eq "SETTLED" } | Select-Object -First 1
if ($settled) {
    $d5 = Invoke-Api -Method GET -Path "/api/trips/$($settled.tripId)/settlement/result" -Token $Riya.accessToken
    Write-TestResult "D5" "GET settlement result" (-not $d5.IsError) "200"
} else {
    Write-TestResult "D5" "GET settlement result" $true "skipped (no SETTLED trip)"
}

Write-Host "`n========================================"
if ($Failed -eq 0) {
    Write-Host "API TRIGGER + REGRESSION TESTS PASSED"
    Write-Host "For live WebSocket verify: use browser console script in Step 9 spec with token=$($Riya.accessToken.Substring(0,20))..."
    exit 0
} else {
    Write-Host "$Failed TEST(S) FAILED"
    exit 1
}
