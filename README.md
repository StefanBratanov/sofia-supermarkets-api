# sofia-supermarkets-api

[![build](https://github.com/StefanBratanov/sofia-supermarkets-api/actions/workflows/build.yml/badge.svg)](https://github.com/StefanBratanov/sofia-supermarkets-api/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=StefanBratanov_sofia-supermarkets-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=StefanBratanov_sofia-supermarkets-api)
[![codecov](https://codecov.io/gh/StefanBratanov/sofia-supermarkets-api/branch/master/graph/badge.svg?token=3V3THIY6AX)](https://codecov.io/gh/StefanBratanov/sofia-supermarkets-api)

API за извличане на информация за продукти от супермаркети в София.

[![Swagger UI](https://validator.swagger.io/validator?url=https://api.naoferta.net/v3/api-docs)](https://api.naoferta.net/swagger-ui.html)

## Сайтове, използващи това API

- [Алкохол на оферта](https://naoferta.net/)

## Поддържани супермаркети

- [x] Billa

> Получава информация за продуктите от https://ssbbilla.site/. Изображенията се извличат въз основа
> на
> първия резултат в търсенето на изображения в Google

- [x] Fantastico

> Изтегля PDF брошурите от https://www.fantastico.bg/special-offers и анализира информацията за
> продуктите в тях. Изображенията се извличат въз основа на първия резултат в търсенето на
> изображения
> в Google.

- [x] Kaufland

> Получава информация за продуктите от https://www.kaufland.bg/.

- [x] Lidl

> Получава информация за продуктите от https://www.lidl.bg/.

- [x] T-Market

> Получава информация за продуктите от https://tmarketonline.bg/.

- [ ] METRO
- [ ] Kam Market
- [ ] CBA
- [ ] ProMarket
- [ ] Hit Max

## Tech Stack

* Kotlin
* Gradle
* Spring Boot

## Code Style

Kotlin code conventions, based on [ktfmt](https://github.com/facebook/ktfmt)
and [google-java-format](https://github.com/google/google-java-format) are used in this project. To
reformat code, run:

```
./gradlew spotlessApply
```

## Running Locally

Install JDK 17 or above from [here](https://jdk.java.net/).

* **Unix**

```
./gradlew bootRun
```

* **Windows**

```
gradlew.bat bootRun
```

The following environment variables need to be set prior to running:

* DB_URL
* DB_USERNAME
* DB_PASSWORD
* GOOGLE_API_KEY
* CLOUDINARY_API_SECRET

Navigate to <http://localhost:8080/swagger-ui.html> to check the API documentation and
test the endpoints.

![API Documentation](images/swagger-ui.png)
