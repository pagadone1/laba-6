#!/bin/bash
# Генерация цепочки сертификатов для Car Service (минимум 3 звена)
# Цепочка: Root CA -> Intermediate CA -> Server certificate
# ВАЖНО: Замените STUDENT_ID на номер вашего студенческого билета!

set -e

STUDENT_ID="${STUDENT_ID:-STUDENT_ID}"
CERT_DIR="${CERT_DIR:-./src/main/resources/certs}"
KEYSTORE_PASS="${KEYSTORE_PASSWORD:-changeit}"
KEY_PASS="${KEYSTORE_PASSWORD:-changeit}"

mkdir -p "$CERT_DIR"
cd "$CERT_DIR"

# --- 1. Root CA (корневой сертификат) ---
# Имена отличны от стандартных примеров (не ca.example.com)
echo "=== 1. Генерация Root CA (carservice-root-ca) ==="
openssl genrsa -out carservice-root-ca.key 4096
openssl req -x509 -new -nodes -key carservice-root-ca.key -sha256 -days 7300 \
    -out carservice-root-ca.crt \
    -subj "/C=RU/ST=Moscow/L=Moscow/O=CarService PO6/OU=Root CA/CN=carservice-root-ca/ serialNumber=STUDENT-${STUDENT_ID}"

# --- 2. Intermediate CA ---
echo "=== 2. Генерация Intermediate CA ==="
openssl genrsa -out carservice-intermediate.key 4096
openssl req -new -key carservice-intermediate.key \
    -out carservice-intermediate.csr \
    -subj "/C=RU/ST=Moscow/L=Moscow/O=CarService PO6/OU=Intermediate CA/CN=carservice-intermediate-ca/ serialNumber=STUDENT-${STUDENT_ID}"

# Создаём extension для Intermediate CA
cat > intermediate-ext.cnf << 'EOF'
basicConstraints=CA:TRUE,pathlen:1
keyUsage=keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid:always,issuer
EOF

openssl x509 -req -in carservice-intermediate.csr \
    -CA carservice-root-ca.crt -CAkey carservice-root-ca.key \
    -CAcreateserial -out carservice-intermediate.crt -days 3650 -sha256 \
    -extfile intermediate-ext.cnf
rm carservice-intermediate.csr intermediate-ext.cnf

# --- 3. Server certificate ---
echo "=== 3. Генерация серверного сертификата ==="
openssl genrsa -out carservice-server.key 2048
openssl req -new -key carservice-server.key \
    -out carservice-server.csr \
    -subj "/C=RU/ST=Moscow/L=Moscow/O=CarService PO6/OU=Server/CN=localhost/ serialNumber=STUDENT-${STUDENT_ID}"

# SAN для localhost
cat > server-ext.cnf << 'EOF'
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=DNS:localhost,IP:127.0.0.1
EOF

openssl x509 -req -in carservice-server.csr \
    -CA carservice-intermediate.crt -CAkey carservice-intermediate.key \
    -CAcreateserial -out carservice-server.crt -days 365 -sha256 \
    -extfile server-ext.cnf
rm carservice-server.csr server-ext.cnf

# --- 4. Keystore (PKCS12) для Spring Boot ---
echo "=== 4. Создание keystore ==="
cat carservice-server.crt carservice-intermediate.crt carservice-root-ca.crt > fullchain.crt
openssl pkcs12 -export -in fullchain.crt -inkey carservice-server.key \
    -out carservice-keystore.p12 -name carservice-server \
    -passout "pass:${KEYSTORE_PASS}" -CAfile carservice-root-ca.crt

echo ""
echo "=== Готово! Сертификаты созданы в $CERT_DIR ==="
echo "Цепочка: carservice-root-ca -> carservice-intermediate-ca -> carservice-server"
echo "Keystore: carservice-keystore.p12"
echo ""
echo "Добавьте carservice-root-ca.crt в доверенные корневые сертификаты системы/браузера."
