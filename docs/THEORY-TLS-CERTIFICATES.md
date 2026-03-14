# Теория: TLS и цепочки сертификатов

## Иерархия PKI (Public Key Infrastructure)

Цепочка сертификатов строится по иерархической модели:

1. **Root CA (корневой центр сертификации)** — самоподписанный сертификат, основа доверия
2. **Intermediate CA (промежуточный CA)** — подписан Root CA
3. **End-entity (серверный сертификат)** — подписан Intermediate CA

## Наш случай (3 звена)

```
carservice-root-ca (self-signed)
        |
        v
carservice-intermediate-ca (signed by root)
        |
        v
carservice-server (signed by intermediate)
```

## Идентификатор студента в сертификате

В каждом сертификате цепочки присутствует `serialNumber=STUDENT-<номер_студбилета>` в Subject DN.

## TLS в Spring Boot

- Keystore (PKCS12) содержит приватный ключ сервера и цепочку сертификатов
- `server.ssl.*` настраивает порт 8443 для HTTPS
- Браузеру нужен Root CA в доверенных, чтобы не показывать предупреждение

## Добавление в доверенные

### Windows
1. Двойной клик по `carservice-root-ca.crt`
2. «Установить сертификат» → «Текущий пользователь»
3. «Поместить все сертификаты в следующее хранилище» → «Доверенные корневые центры сертификации»

### Linux
```bash
sudo cp carservice-root-ca.crt /usr/local/share/ca-certificates/
sudo update-ca-certificates
```

### Браузер Chrome
Импортировать `carservice-root-ca.crt` через настройки → Безопасность → Управление сертификатами → Доверенные корневые ЦС → Импорт.
