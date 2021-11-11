[![build](https://github.com/StefanBratanov/sofia-supermarkets-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/StefanBratanov/sofia-supermarkets-api/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/StefanBratanov/sofia-supermarkets-api/branch/master/graph/badge.svg?token=3V3THIY6AX)](https://codecov.io/gh/StefanBratanov/sofia-supermarkets-api)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/stefanbratanov/sofia-supermarkets-api)](https://github.com/StefanBratanov/sofia-supermarkets-api/releases/latest)

# sofia-supermarkets-api
An API to retrieve products information from supermarkets in Sofia.

[![Swagger UI](https://validator.swagger.io/validator?url=http://sofiasupermarketsapi-1888309410.eu-west-2.elb.amazonaws.com/v3/api-docs/)](http://sofiasupermarketsapi-1888309410.eu-west-2.elb.amazonaws.com/swagger-ui.html)

If you like my work, you can buy me a coffee.

<a target="_blank" href="https://www.buymeacoffee.com/stefanbratanov"><img src="https://img.buymeacoffee.com/button-api/?text=Buy me a coffee&emoji=&slug=stefanbratanov&button_colour=FFDD00&font_colour=000000&font_family=Lato&outline_colour=000000&coffee_colour=ffffff"></a>

## Supported supermarkets

- [x] Billa
> Gets products information from https://ssbbilla.site/. Images are retrieved based on the first result in google image search.
- [x] Fantastico
> Downloads the pdf brochures available on https://www.fantastico.bg/special-offers and parses the products information in them. Images are retrieved based on the first result in google image search.
- [x] Kaufland
> Gets products information from https://www.kaufland.bg/.
- [x] Lidl
> Gets products information from https://www.lidl.bg/.
- [x] T-Market
> Gets products information from https://tmarketonline.bg/.
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

Navigate to <http://localhost:8080/swagger-ui.html> to check the API documentation and test the endpoints

![API Documentation](images/swagger-ui.png)
