# Настройка TLS и цепочки сертификатов

## 1. Генерация цепочки сертификатов (3 звена)

**Требования:** OpenSSL (в PATH). Windows: установите [OpenSSL](https://slproweb.com/products/Win32OpenSSL.html) или используйте Git Bash.

**Обязательно задайте номер студенческого билета:**

### Linux / macOS (Git Bash)
```bash
cd demo/scripts
export STUDENT_ID=123456   # Ваш номер студбилета
./generate-certificates.sh
```

### Windows (PowerShell)
```powershell
cd demo\scripts
$env:STUDENT_ID = "123456"   # Ваш номер студбилета
.\generate-certificates.ps1
```

Сертификаты создаются в `demo/src/main/resources/certs/`:
- `carservice-root-ca.crt` — корневой CA
- `carservice-intermediate.crt` — промежуточный CA
- `carservice-server.crt` — серверный сертификат
- `carservice-keystore.p12` — keystore для Spring Boot

Идентификатор студента (`serialNumber=STUDENT-<номер>`) включён в Subject каждого сертификата.

## 2. Добавление Root CA в доверенные (для браузера)

### Windows
1. Откройте `demo/src/main/resources/certs/carservice-root-ca.crt` двойным кликом
2. «Установить сертификат» → «Текущий пользователь» → Далее
3. «Поместить все сертификаты в следующее хранилище» → «Обзор»
4. Выберите «Доверенные корневые центры сертификации» → OK → Далее → Готово

### Linux
```bash
sudo cp demo/src/main/resources/certs/carservice-root-ca.crt /usr/local/share/ca-certificates/carservice-root-ca.crt
sudo update-ca-certificates
```

### macOS
```bash
sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain demo/src/main/resources/certs/carservice-root-ca.crt
```

### Chrome (отдельно)
Настройки → Конфиденциальность и безопасность → Безопасность → Управление сертификатами → Доверенные корневые ЦС → Импорт → выберите `carservice-root-ca.crt`

## 3. Запуск сервиса с TLS

Скопируйте `.env.example` в `.env` и добавьте:

```
SSL_KEY_STORE=file:./src/main/resources/certs/carservice-keystore.p12
SSL_KEY_STORE_PASSWORD=changeit
```

Запуск с профилем `tls`:
```bash
cd demo
./mvnw spring-boot:run -Dspring-boot.run.profiles=tls
```

Сервис будет доступен по **https://localhost:8443**

## 4. CI: хранение keystore и пароля

### GitHub Secrets
Settings → Secrets and variables → Actions → New repository secret:
- `KEYSTORE_BASE64` — содержимое keystore в base64: `base64 -w0 carservice-keystore.p12`
- `KEYSTORE_PASSWORD` — пароль keystore (Masked)

### GitLab Variables
Settings → CI/CD → Variables → Add variable:
- `KEYSTORE_BASE64` — File, Protected, содержимое keystore
- `KEYSTORE_PASSWORD` — Variable, Protected, Masked — пароль

## 5. Безопасность

- **Не коммитить** ключи (`.key`), keystore (`.p12`, `.jks`), пароли, сертификаты
- В `.gitignore` исключены: `*.key`, `*.p12`, `*.jks`, `*.keystore`, `**/certs/*`
