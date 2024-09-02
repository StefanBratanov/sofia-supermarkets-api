import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion = "2.0.0"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.jpa") version kotlinVersion
  id("org.springframework.boot") version "3.3.0"
  id("io.spring.dependency-management") version "1.1.0"
  id("me.qoomon.git-versioning") version "6.4.1"
  id("com.diffplug.spotless") version "6.25.0"
  id("jacoco")
}

group = "com.stefanbratanov"

version = "develop"

gitVersioning.apply {
  refs {
    tag("v(?<version>.*)") { version = "\${ref.version}" }
    rev { version = "develop" }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

jacoco { toolVersion = "0.8.8" }

repositories { mavenCentral() }

// version compatible with PhantomJS
val seleniumVersion = "4.8.1"
// https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#appendix.dependency-versions
extra["selenium.version"] = seleniumVersion

val junitVersion = "5.11.0"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.data:spring-data-keyvalue")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.postgresql:postgresql")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.apache.commons:commons-lang3")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.apache.commons:commons-math3:3.6.1")
  implementation("org.jsoup:jsoup:1.18.1")
  implementation("org.apache.pdfbox:pdfbox:3.0.0")
  implementation("me.xdrop:fuzzywuzzy:1.4.0")
  implementation("com.cloudinary:cloudinary-http45:1.39.0")
  implementation("org.seleniumhq.selenium:selenium-java:$seleniumVersion")
  implementation("com.codeborne:phantomjsdriver:1.5.0")
  implementation("io.github.bonigarcia:webdrivermanager:4.4.3") // no PhantomJS driver in 5.x.x
  testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.0")
  testImplementation("io.mockk:mockk:1.13.4")
  testImplementation("com.ninja-squad:springmockk:4.0.0")
  testImplementation("org.skyscreamer:jsonassert:1.5.1")
  testImplementation("org.mock-server:mockserver-spring-test-listener:5.15.0")
  testRuntimeOnly("com.h2database:h2")
}

springBoot { buildInfo() }

// dont't produce *-plain.jar
tasks.getByName<Jar>("jar") { enabled = false }

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging { showStandardStreams = true }
}

configure<SpotlessExtension> { kotlin { ktfmt().googleStyle() } }

tasks.jacocoTestReport { reports { xml.required.set(true) } }
