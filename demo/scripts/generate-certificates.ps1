# Генерация цепочки сертификатов для Car Service (минимум 3 звена)
# Root CA -> Intermediate CA -> Server certificate
# ВАЖНО: Замените $env:STUDENT_ID на номер вашего студенческого билета!

$ErrorActionPreference = "Stop"
$StudentId = if ($env:STUDENT_ID) { $env:STUDENT_ID } else { "STUDENT_ID" }
$CertDir = if ($env:CERT_DIR) { $env:CERT_DIR } else { Join-Path $PSScriptRoot "..\src\main\resources\certs" }
$KeyStorePass = if ($env:KEYSTORE_PASSWORD) { $env:KEYSTORE_PASSWORD } else { "changeit" }

New-Item -ItemType Directory -Force -Path $CertDir | Out-Null
Push-Location $CertDir

try {
    # --- 1. Root CA ---
    Write-Host "=== 1. Root CA (carservice-root-ca) ==="
    openssl genrsa -out carservice-root-ca.key 4096
    openssl req -x509 -new -nodes -key carservice-root-ca.key -sha256 -days 7300 `
        -out carservice-root-ca.crt `
        -subj "/C=RU/ST=Moscow/L=Moscow/O=CarService PO6/OU=Root CA/CN=carservice-root-ca/ serialNumber=STUDENT-$StudentId"

    # --- 2. Intermediate CA ---
    Write-Host "=== 2. Intermediate CA ==="
    openssl genrsa -out carservice-intermediate.key 4096
    openssl req -new -key carservice-intermediate.key `
        -out carservice-intermediate.csr `
        -subj "/C=RU/ST=Moscow/L=Moscow/O=CarService PO6/OU=Intermediate CA/CN=carservice-intermediate-ca/ serialNumber=STUDENT-$StudentId"

    Set-Content -Path intermediate-ext.cnf -Encoding ASCII -Value @(
        "basicConstraints=CA:TRUE,pathlen:1",
        "keyUsage=keyCertSign,cRLSign",
        "subjectKeyIdentifier=hash",
        "authorityKeyIdentifier=keyid:always,issuer"
    )

    openssl x509 -req -in carservice-intermediate.csr `
        -CA carservice-root-ca.crt -CAkey carservice-root-ca.key `
        -CAcreateserial -out carservice-intermediate.crt -days 3650 -sha256 `
        -extfile intermediate-ext.cnf
    Remove-Item carservice-intermediate.csr, intermediate-ext.cnf -Force

    # --- 3. Server certificate ---
    Write-Host "=== 3. Server certificate ==="
    openssl genrsa -out carservice-server.key 2048
    openssl req -new -key carservice-server.key `
        -out carservice-server.csr `
        -subj "/C=RU/ST=Moscow/L=Moscow/O=CarService PO6/OU=Server/CN=localhost/ serialNumber=STUDENT-$StudentId"

    Set-Content -Path server-ext.cnf -Encoding ASCII -Value @(
        "authorityKeyIdentifier=keyid,issuer",
        "basicConstraints=CA:FALSE",
        "keyUsage=digitalSignature,keyEncipherment",
        "extendedKeyUsage=serverAuth",
        "subjectAltName=DNS:localhost,IP:127.0.0.1"
    )

    openssl x509 -req -in carservice-server.csr `
        -CA carservice-intermediate.crt -CAkey carservice-intermediate.key `
        -CAcreateserial -out carservice-server.crt -days 365 -sha256 `
        -extfile server-ext.cnf
    Remove-Item carservice-server.csr, server-ext.cnf -Force

    # --- 4. Keystore для Spring Boot ---
    Write-Host "=== 4. Keystore ==="
    Get-Content carservice-server.crt, carservice-intermediate.crt, carservice-root-ca.crt | Set-Content fullchain.crt
    openssl pkcs12 -export -in fullchain.crt -inkey carservice-server.key `
        -out carservice-keystore.p12 -name carservice-server `
        -passout "pass:$KeyStorePass" -CAfile carservice-root-ca.crt

    Write-Host ""
    Write-Host "=== Done! Certificates in $CertDir ==="
    Write-Host "Chain: carservice-root-ca -> carservice-intermediate-ca -> carservice-server"
    Write-Host "Keystore: carservice-keystore.p12"
    Write-Host "Add carservice-root-ca.crt to trusted root store."
}
finally {
    Pop-Location
}
