
# 🛡️ Приложение использует схему аутентификации и регистрации на основе Access Token + Refresh Token +CAPTCHA:

- **Access Token** — живёт недолго (30 минут) и используется для доступа к защищённым API-эндпоинтам.
  - Токен не сохраняются в базе данных.  
  - Проверка  осуществляется посредством парсинга: проверяются подпись и срок действия. Если токен успешно распарсен, значит его подпись действительна и срок действия ещё не истек.
  - Позволяет избежать частого ввода пароля, улучшая пользовательский опыт UX (впечатлении и реакции пользователя при взаимодействии с продуктом).

- **Refresh Token** — живёт долго (30 дней), хранится в **Redis**, и позволяет обновлять сессию без повторного входа.
   - Привязан к пользователю.
   - Автоматически удаляется по истечении срока или при выходе из системы.

- **CAPTCHA** — обязательна при регистрации.
   - Защищает от автоматических ботов.
   - Пользователь решает CAPTCHA → получает одноразовый токен → регистрируется только с этим токеном.
   - Токен удаляется сразу после использования (защита от повторной отправки).

---
## Запуск проекта

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/Katas77/access-guard.git
   cd contacts-application
   ```
2. Запустите redis и  postgres через Docker:
   ```bash
   cd docker

   ```
   ```bash
   docker-compose up -d
   ```

### 🔁 Этот класс клиент  тестирования.  Он отправляет HTTP-запросы к нашему  Spring Boot-приложению и обрабатывает полученные ответы.

```java
package com.example.applicationRoma.clientOk;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AuthClientExample {

  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private static final OkHttpClient client = new OkHttpClient.Builder()
          .connectTimeout(10, TimeUnit.SECONDS)
          .readTimeout(30, TimeUnit.SECONDS)
          .callTimeout(1, TimeUnit.MINUTES)
          .build();

  private static final ObjectMapper mapper = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private static final String BASE_URL = "http://localhost:8080";

  private static final String TEST_EMAIL = "roma@example.com";
  private static final String TEST_PASSWORD = "Password";

  public static void main(String[] args) {
    System.out.println("=== Регистрация ===");
    registerUser();
    System.out.println("\n=== Логин ===");
    AuthResponse loginResponse = loginUser();
    if (loginResponse == null) {
      System.err.println("Ошибка входа — прерывание");
      return;
    }
    System.out.println("\n=== Обновление токена ===");
    AuthResponse refreshResponse = refreshToken(loginResponse.refreshToken());
    if (refreshResponse == null) {
      System.err.println("Ошибка обновления токена — прерывание");
      return;
    }
    System.out.println("\n=== Валидация токена ===");
    validateToken(refreshResponse.accessToken());
  }

  private static void registerUser() {
    CreateUserRequest request = new CreateUserRequest(
            TEST_EMAIL,
            TEST_PASSWORD,
            "Роман",
            "token"
    );

    try {
      String json = mapper.writeValueAsString(request);
      RequestBody body = RequestBody.create(json, JSON);
      Request httpRequest = new Request.Builder()
              .url(BASE_URL + "/api/v1/auth/register")
              .post(body)
              .build();

      String responseBody = executeRequest(httpRequest);
      System.out.println("Ответ регистрации: " + responseBody);

    } catch (IOException e) {
      System.err.println("Ошибка регистрации: " + e.getMessage());
    }
  }

  private static AuthResponse loginUser() {
    LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
    try {
      String json = mapper.writeValueAsString(request);
      RequestBody body = RequestBody.create(json, JSON);
      Request httpRequest = new Request.Builder()
              .url(BASE_URL + "/api/v1/auth/login")
              .post(body)
              .build();

      String responseBody = executeRequest(httpRequest);
      System.out.println("Ответ логина: " + responseBody);
      return mapper.readValue(responseBody, AuthResponse.class);

    } catch (IOException e) {
      System.err.println("Ошибка логина: " + e.getMessage());
      return null;
    }
  }

  private static AuthResponse refreshToken(String refreshToken) {
    RefreshRequest request = new RefreshRequest(refreshToken);
    try {
      String json = mapper.writeValueAsString(request);
      RequestBody body = RequestBody.create(json, JSON);
      Request httpRequest = new Request.Builder()
              .url(BASE_URL + "/api/v1/auth/refresh")
              .post(body)
              .build();

      String responseBody = executeRequest(httpRequest);
      System.out.println("Ответ refresh: " + responseBody);
      return mapper.readValue(responseBody, AuthResponse.class);

    } catch (IOException e) {
      System.err.println("Ошибка refresh: " + e.getMessage());
      return null;
    }
  }

  private static void validateToken(String accessToken) {
    Request httpRequest = new Request.Builder()
            .url(BASE_URL + "/api/v1/test")
            .header("Authorization", "Bearer " + accessToken)
            .get()
            .build();

    try {
      String responseBody = executeRequest(httpRequest);
      System.out.println("Ответ от защищённого эндпоинта: " + responseBody);
    } catch (IOException e) {
      System.err.println("Ошибка при валидации токена: " + e.getMessage());
    }
  }

  private static String executeRequest(Request request) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      ResponseBody rb = response.body();
      String body = rb != null ? rb.string() : "";

      System.out.println("HTTP " + response.code());

      if (!response.isSuccessful()) {
        throw new IOException("HTTP " + response.code() + ": " + body);
      }

      return body;
    }
  }
  public record CreateUserRequest(String email, String password1, String name, String captchaToken) {}
  public record LoginRequest(String email, String password) {}
  public record RefreshRequest(String refreshToken) {}
  public record AuthResponse(String accessToken, String refreshToken) {}

}

```

### ⚙️ Технологии

- **Spring Boot 3.5+**, **Spring Security**
- **JWT** (библиотека `jjwt`) — для генерации и проверки access token
- **Redis** — для хранения refresh token и CAPTCHA-данных (с TTL и автоматической очисткой)
- **Kaptcha** — генерация изображений CAPTCHA


---

### ✅ Преимущества

- 🔒 **Безопасно**:  
  короткий access token, обязательная CAPTCHA при регистрации, refresh token хранится только в Redis с привязкой к пользователю.
- 🔄 **Удобно для пользователя**:  
  автоматическое обновление сессии без повторного ввода логина/пароля.
- 🧹 **Чистая память**:  
  все данные в Redis удаляются автоматически по TTL или при logout — нет утечек.
- 📦 **Готово к масштабированию**:  
  stateless архитектура + централизованное хранилище (Redis) позволяют легко разворачивать несколько инстансов.
- 🌐 **Совместимо с микросервисами**:  
  работает с **Spring Cloud Gateway**

---

✉ **Контакты**: [krp77@mail.ru](mailto:krp77@mail.ru)

