param([string]$UnsignedApk = 'release/niu-1.1.0-unsigned.apk', [string]$SignedApk = 'release/niu-1.1.0-android.apk')
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
$taskSigning = Get-Content (Join-Path $taskRoot '.private/signing.json') -Raw | ConvertFrom-Json
$env:NIU_STORE_PASS = $taskSigning.storePassword
$env:NIU_KEY_PASS = $taskSigning.keyPassword
try {
    & java -jar (Join-Path $taskRoot '.private/apksigner.jar') sign --ks (Join-Path $taskRoot '.private/niu-release.jks') --ks-key-alias funddiary --ks-pass env:NIU_STORE_PASS --key-pass env:NIU_KEY_PASS --out (Join-Path $taskRoot $SignedApk) (Join-Path $taskRoot $UnsignedApk)
    if ($LASTEXITCODE -ne 0) { throw '签名失败' }
    & java -jar (Join-Path $taskRoot '.private/apksigner.jar') verify --verbose --print-certs (Join-Path $taskRoot $SignedApk)
    if ($LASTEXITCODE -ne 0) { throw '签名验证失败' }
} finally {
    Remove-Item Env:NIU_STORE_PASS -ErrorAction SilentlyContinue
    Remove-Item Env:NIU_KEY_PASS -ErrorAction SilentlyContinue
}
