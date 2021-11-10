[![build](https://github.com/Stefata/sofia-supermarkets-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/Stefata/sofia-supermarkets-api/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/Stefata/sofia-supermarkets-api/branch/master/graph/badge.svg?token=3V3THIY6AX)](https://codecov.io/gh/Stefata/sofia-supermarkets-api)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/stefata/sofia-supermarkets-api)](https://github.com/Stefata/sofia-supermarkets-api/releases/latest)

# sofia-supermarkets-api
An API to retrieve products information from supermarkets in Sofia

[![Swagger UI](https://validator.swagger.io/validator?url=http://sofiasupermarketsapi-1888309410.eu-west-2.elb.amazonaws.com/v3/api-docs)](http://sofiasupermarketsapi-1888309410.eu-west-2.elb.amazonaws.com/swagger-ui.html)

## Supported supermarkets

- [x] Billa
> Gets product information from https://ssbbilla.site/. Images are retrieved based on the first result in google image search.
- [x] Fantastico
> Downloads the pdf brochures available on https://www.fantastico.bg/special-offers and parses the product information from them. Images are retrieved based on the first result in google image search.
- [x] Kaufland
> Gets product information from https://www.kaufland.bg/.
- [x] Lidl
> Gets product information from https://www.lidl.bg/.
- [x] T-Market
> Gets product information from https://tmarketonline.bg/.
- [ ] METRO
- [ ] Kam Market
- [ ] CBA
- [ ] ProMarket
- [ ] Hit Max

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
* CLOUDINARY_API_SECRET

Navigate to <http://localhost:8080/swagger-ui/> to check the API documentation and test the endpoints

![API Documentation](images/swagger-ui.png)
