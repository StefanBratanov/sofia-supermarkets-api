[![build](https://github.com/Stefata/sofia-supermarkets-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/Stefata/sofia-supermarkets-api/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/Stefata/sofia-supermarkets-api/branch/master/graph/badge.svg?token=3V3THIY6AX)](https://codecov.io/gh/Stefata/sofia-supermarkets-api)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/stefata/sofia-supermarkets-api)](https://github.com/Stefata/sofia-supermarkets-api/releases/latest)

# sofia-supermarkets-api
An API to retrieve products information from supermarkets in Sofia

## Tech Stack
* Kotlin
* Gradle  
* Spring Boot

## Running Locally
Install JDK 15 or above from [here](https://jdk.java.net/)
* **Unix** `./gradlew bootRun`
* **Windows** `gradlew.bat bootRun`

The following environment variables need to be set prior to running
* DB_USERNAME
* DB_PASSWORD
* GOOGLE_API_KEY

Navigate to <http://localhost:8080/swagger-ui/> to check the API documentation and test the endpoints

![API Documentation](images/swagger-ui.png)

## Supported supermarkets

- [x] Billa
- [x] Fantastico
- [x] Kaufland
- [x] Lidl
- [x] T-Market
- [ ] METRO  
- [ ] Kam Market
- [ ] CBA
- [ ] ProMarket
- [ ] Hit Max
