$ErrorActionPreference = "Stop"
$Base = "http://localhost:8080"

function Write-TestResult($num, $name, $pass, $detail) {
    $status = if ($pass) { "PASS" } else { "FAIL" }
    Write-Host ("[{0}] TEST {1}: {2} - {3}" -f $status, $num, $name, $detail)
    if (-not $pass) { $script:Failed++ }
}

$Failed = 0

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token,
        $Body = $null
    )
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $uri = "$Base$Path"
    $params = @{
        Uri         = $uri
        Method      = $Method
        Headers     = $headers
        ErrorAction = "Stop"
    }
    if ($null -ne $Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10 -Compress)
    }
    try {
        $data = Invoke-RestMethod @params
        return [PSCustomObject]@{ StatusCode = 200; Json = $data; IsError = $false; ApiError = $null }
    } catch {
        $status = 0
        $apiError = $_.Exception.Message
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode.value__
            try {
                $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
                $body = $reader.ReadToEnd()
                $reader.Close()
                if ($body) {
                    $parsed = $body | ConvertFrom-Json
                    if ($parsed.error) { $apiError = [string]$parsed.error }
                    else { $apiError = $body }
                }
            } catch { }
        } elseif ($_.ErrorDetails.Message) {
            try {
                $parsed = $_.ErrorDetails.Message | ConvertFrom-Json
                if ($parsed.error) { $apiError = [string]$parsed.error }
            } catch { }
        }
        return [PSCustomObject]@{
            StatusCode = $status
            Json       = $null
            IsError    = $true
            ApiError   = $apiError
        }
    }
}

function Get-Data($Response) {
    return $Response.Json.data
}

function Register-Or-Login {
    param([string]$Name, [string]$Phone, [string]$Email, [string]$Password)
    $regBody = @{
        name     = $Name
        phone    = $Phone
        email    = $Email
        password = $Password
    }
    try {
        $r = Invoke-Api -Method POST -Path "/api/auth/register" -Body $regBody
    } catch {
        # already registered
    }
    $loginBody = @{ phoneOrEmail = $Phone; password = $Password }
    $r = Invoke-Api -Method POST -Path "/api/auth/login" -Body $loginBody
    if (-not $r.Json.success) { throw "Login failed for $Name : $($r.Json.error)" }
    return $r.Json.data
}

function Bootstrap-Step7Trip {
    param($Riya, $Arjun)
    $createBody = @{
        name        = "Step8 Settlement Trip $(Get-Date -Format 'yyyyMMddHHmmss')"
        destination = "Goa"
        kittyTarget = 0
    }
    $r = Invoke-Api -Method POST -Path "/api/trips" -Token $Riya.accessToken -Body $createBody
    $trip = Get-Data $r
    $tripId = $trip.tripId

    $inviteBody = @{ phone = "9876500002"; role = "MEMBER" }
    Invoke-Api -Method POST -Path "/api/trips/$tripId/invite" -Token $Riya.accessToken -Body $inviteBody | Out-Null

    $deposit = @{ amount = 0; method = "UPI" }
    $deposit.amount = 8000
    Invoke-Api -Method POST -Path "/api/trips/$tripId/kitty/deposit" -Token $Riya.accessToken -Body $deposit | Out-Null
    $deposit.amount = 9200
    Invoke-Api -Method POST -Path "/api/trips/$tripId/kitty/deposit" -Token $Arjun.accessToken -Body $deposit | Out-Null

    $statusBody = @{ newStatus = "ACTIVE" }
    Invoke-Api -Method PATCH -Path "/api/trips/$tripId/status" -Token $Riya.accessToken -Body $statusBody | Out-Null

    $hotel = @{
        title        = "Hotel"
        amount       = 7000
        equalSplit   = $true
        paidFrom     = "KITTY"
        participants = @(
            @{ userId = $Riya.userId },
            @{ userId = $Arjun.userId }
        )
    }
    Invoke-Api -Method POST -Path "/api/trips/$tripId/expenses" -Token $Riya.accessToken -Body $hotel | Out-Null

    $dinner = @{
        title        = "Beach dinner"
        amount       = 2800
        equalSplit   = $false
        paidFrom     = "KITTY"
        participants = @(
            @{ userId = $Riya.userId; share = 1000 },
            @{ userId = $Arjun.userId; share = 1800 }
        )
    }
    Invoke-Api -Method POST -Path "/api/trips/$tripId/expenses" -Token $Riya.accessToken -Body $dinner | Out-Null

    $extBody = @{
        expenseId = (Get-Data (Invoke-Api -Method GET -Path "/api/trips/$tripId/expenses" -Token $Riya.accessToken)).expenses[0].expenseId
        amount    = 1500
        method    = "UPI"
    }
    Invoke-Api -Method POST -Path "/api/trips/$tripId/kitty/external-payment" -Token $Arjun.accessToken -Body $extBody | Out-Null

    return $tripId
}

function Bootstrap-DeficitTrip {
    param($Riya, $Arjun)
    $createBody = @{
        name        = "Step8 Deficit Trip $(Get-Date -Format 'yyyyMMddHHmmss')"
        destination = "Test"
        kittyTarget = 0
    }
    $r = Invoke-Api -Method POST -Path "/api/trips" -Token $Riya.accessToken -Body $createBody
    $tripId = (Get-Data $r).tripId

    Invoke-Api -Method POST -Path "/api/trips/$tripId/invite" -Token $Riya.accessToken -Body @{ phone = "9876500002"; role = "MEMBER" } | Out-Null

    $deposit = @{ amount = 5000; method = "UPI" }
    Invoke-Api -Method POST -Path "/api/trips/$tripId/kitty/deposit" -Token $Riya.accessToken -Body $deposit | Out-Null
    $deposit.amount = 1000
    Invoke-Api -Method POST -Path "/api/trips/$tripId/kitty/deposit" -Token $Arjun.accessToken -Body $deposit | Out-Null

    Invoke-Api -Method PATCH -Path "/api/trips/$tripId/status" -Token $Riya.accessToken -Body @{ newStatus = "ACTIVE" } | Out-Null

    $expense = @{
        title          = "Group expense"
        amount         = 8000
        paidByUserId   = $Riya.userId
        equalSplit     = $true
        paidFrom       = "KITTY"
        participants   = @(
            @{ userId = $Riya.userId },
            @{ userId = $Arjun.userId }
        )
    }
    Invoke-Api -Method POST -Path "/api/trips/$tripId/expenses" -Token $Riya.accessToken -Body $expense | Out-Null

    Invoke-Api -Method PATCH -Path "/api/trips/$tripId/status" -Token $Riya.accessToken -Body @{ newStatus = "FROZEN" } | Out-Null
    return $tripId
}

# Health check
try {
    $h = Invoke-Api -Method GET -Path "/api/health"
    if ($h.StatusCode -ne 200) { throw "Health check failed" }
    # StatusCode set by wrapper
    Write-Host "Server is up at $Base`n"
} catch {
    Write-Host "ERROR: Cannot reach $Base - start Spring Boot and MySQL first."
    exit 1
}

$pwd = "password1"
$Riya = Register-Or-Login -Name "Riya" -Phone "9876500001" -Email "riya.step8@test.com" -Password $pwd
$Arjun = Register-Or-Login -Name "Arjun" -Phone "9876500002" -Email "arjun.step8@test.com" -Password $pwd
$Priya = Register-Or-Login -Name "Priya" -Phone "9876500003" -Email "priya.step8@test.com" -Password $pwd

Write-Host "Bootstrapping Step 7 equivalent trip (ACTIVE)..."
$tripId = Bootstrap-Step7Trip -Riya $Riya -Arjun $Arjun
Write-Host "Main trip ID: $tripId`n"

# TEST 1
$r1 = Invoke-Api -Method GET -Path "/api/trips/$tripId/settlement/preview" -Token $Riya.accessToken
$ok1 = $r1.IsError -and ($r1.ApiError -match "FROZEN or SETTLED")
Write-TestResult 1 "Preview blocked when ACTIVE" $ok1 $r1.ApiError

# TEST 2
$r2 = Invoke-Api -Method PATCH -Path "/api/trips/$tripId/status" -Token $Riya.accessToken -Body @{ newStatus = "FROZEN" }
$d2 = Get-Data $r2
Write-TestResult 2 "Freeze trip" ($d2.status -eq "FROZEN") "status=$($d2.status)"

# TEST 3
$r3 = Invoke-Api -Method GET -Path "/api/trips/$tripId/settlement/preview" -Token $Riya.accessToken
$p3 = Get-Data $r3
$ok3 = ($p3.mode -eq "REFUND") -and ($p3.totalTransfers -eq 0) -and ($p3.canFinalise -eq $true) -and ($p3.memberBalances.Count -eq 2) -and ($p3.transferInstructions.Count -eq 0) -and ($p3.statusMessage -match "REFUND|surplus")
Write-TestResult 3 "Preview settlement (Riya)" $ok3 "mode=$($p3.mode) transfers=$($p3.totalTransfers) canFinalise=$($p3.canFinalise)"

# TEST 4
$r4 = Invoke-Api -Method GET -Path "/api/trips/$tripId/settlement/preview" -Token $Arjun.accessToken
Write-TestResult 4 "Member can preview" ($r4.Json.success -eq $true) "HTTP 200"

# TEST 5
$r5 = Invoke-Api -Method GET -Path "/api/trips/$tripId/settlement/preview" -Token $Priya.accessToken
Write-TestResult 5 "Non-member denied" ($r5.IsError -and ($r5.ApiError -match "Access denied")) $r5.ApiError

# TEST 6
$r6 = Invoke-Api -Method POST -Path "/api/trips/$tripId/settlement/finalise" -Token $Arjun.accessToken
Write-TestResult 6 "Non-admin cannot finalise" ($r6.IsError -and ($r6.ApiError -match "admin")) $r6.ApiError

# TEST 7
$r7 = Invoke-Api -Method POST -Path "/api/trips/$tripId/settlement/finalise" -Token $Riya.accessToken
$f7 = Get-Data $r7
$ok7 = ($f7.mode -eq "REFUND") -and ($f7.transferInstructions.Count -eq 0) -and ($f7.message -match "SETTLED") -and ($null -ne $f7.finalisedAt)
Write-TestResult 7 "Finalise settlement" $ok7 "mode=$($f7.mode) message=$($f7.message)"

# TEST 8
$r8 = Invoke-Api -Method GET -Path "/api/trips/$tripId" -Token $Riya.accessToken
Write-TestResult 8 "Trip SETTLED" ((Get-Data $r8).status -eq "SETTLED") "status=$((Get-Data $r8).status)"

# TEST 9
$r9 = Invoke-Api -Method GET -Path "/api/trips/$tripId/settlement/result" -Token $Riya.accessToken
Write-TestResult 9 "Get settlement result" ($r9.Json.success -eq $true) "HTTP 200"

# TEST 10
$exp = @{
    title        = "Late expense"
    amount       = 100
    equalSplit   = $true
    participants = @(@{ userId = $Riya.userId })
}
$r10 = Invoke-Api -Method POST -Path "/api/trips/$tripId/expenses" -Token $Riya.accessToken -Body $exp
Write-TestResult 10 "No expense on SETTLED" ($r10.IsError -and ($r10.ApiError -match "ACTIVE")) $r10.ApiError

# TEST 11
$r11 = Invoke-Api -Method POST -Path "/api/trips/$tripId/kitty/deposit" -Token $Riya.accessToken -Body @{ amount = 100; method = "UPI" }
Write-TestResult 11 "No deposit on SETTLED" ($r11.IsError -and ($r11.ApiError -match "FROZEN or SETTLED")) $r11.ApiError

# TEST 12
$r12 = Invoke-Api -Method GET -Path "/api/trips/$tripId/settlement/preview" -Token $Riya.accessToken
$p12 = Get-Data $r12
Write-TestResult 12 "Preview on SETTLED" (($p12.canFinalise -eq $false) -and ($r12.StatusCode -eq 200)) "canFinalise=$($p12.canFinalise)"

# TEST 13
Write-Host "`nBootstrapping deficit trip..."
$deficitTripId = Bootstrap-DeficitTrip -Riya $Riya -Arjun $Arjun
$r13 = Invoke-Api -Method GET -Path "/api/trips/$deficitTripId/settlement/preview" -Token $Riya.accessToken
$p13 = Get-Data $r13
$t0 = $p13.transferInstructions[0]
$ok13 = ($p13.mode -eq "DEFICIT") -and ($p13.totalTransfers -eq 1) -and ($t0.fromUserName -eq "Arjun") -and ($t0.toUserName -eq "Riya") -and ([math]::Abs($t0.amount - 1000) -lt 0.01) -and ($t0.description -match "Arjun pays Riya")
Write-TestResult 13 "Deficit mode preview" $ok13 "mode=$($p13.mode) transfer=$($t0.description)"

Write-Host "`n========================================"
if ($Failed -eq 0) {
    Write-Host "ALL 13 POSTMAN TESTS PASSED"
    exit 0
} else {
    Write-Host "$Failed TEST(S) FAILED"
    exit 1
}
