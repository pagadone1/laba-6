# Настройка PO4

## 1. pgAdmin — создать базу

1. Открой pgAdmin.
2. Подключись к серверу PostgreSQL.
3. Правый клик по **Databases** → **Create** → **Database**.
4. Имя: `carservice_po4`.
5. Save.

Либо открой `pgadmin/create_db_po4.sql` и выполни в Query Tool.

---

## 2. Postman — импорт коллекции

1. Postman → **Import**.
2. Выбери файлы:
   - `postman/Car-Service-PO4.postman_collection.json`
   - `postman/Car-Service-PO4.postman_environment.json`
3. В правом верхнем углу выбери окружение **PO4 local**.
4. Настройки коллекции (Variables):
   - `username` — логин после регистрации (по умолчанию admin)
   - `password` — пароль (по умолчанию SecurePass1!)

---

## 3. Порядок работы

1. Запусти приложение (`CarServiceApplication` или `mvn spring-boot:run`).
2. В Postman выполни **Auth → 1. Register** — создать пользователя.
3. Остальные запросы используют Basic Auth (логин/пароль из Variables).

---

## 4. Отдельно от других проектов

- БД: `carservice_po4` (PO3 мог использовать `carservice` или `carservice_po3`).
- Коллекция: **Car Service PO4**.
- Папка проекта: `PO4 — копия/`.
