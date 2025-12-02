import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  val kotlinVersion = "2.2.0"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.jpa") version kotlinVersion
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.0"
  id("me.qoomon.git-versioning") version "6.4.1"
  id("com.diffplug.spotless") version "8.0.0"
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

jacoco { toolVersion = "0.8.13" }

repositories { mavenCentral() }

val junitVersion = "6.0.0"
extra["junit-jupiter.version"] = junitVersion

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.data:spring-data-keyvalue")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.postgresql:postgresql")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.apache.commons:commons-lang3")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.apache.commons:commons-math3:3.6.1")
  implementation("org.jsoup:jsoup:1.21.1")
  implementation("org.apache.pdfbox:pdfbox:3.0.0")
  implementation("me.xdrop:fuzzywuzzy:1.4.0")
  implementation("com.cloudinary:cloudinary-http45:1.39.0")
  implementation("org.seleniumhq.selenium:selenium-java:4.38.0")
  implementation("io.github.bonigarcia:webdrivermanager:6.3.1")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.0")
  testImplementation("io.mockk:mockk:1.14.2")
  testImplementation("com.ninja-squad:springmockk:4.0.0")
  testImplementation("org.skyscreamer:jsonassert:1.5.1")
  testImplementation("org.mock-server:mockserver-spring-test-listener:5.15.0")
  // temporary
  implementation("com.h2database:h2")
}

springBoot { buildInfo() }

// dont't produce *-plain.jar
tasks.getByName<Jar>("jar") { enabled = false }

tasks.withType<KotlinJvmCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs = listOf("-Xannotation-default-target=param-property")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging { showStandardStreams = true }
}

configure<SpotlessExtension> { kotlin { ktfmt("0.56").googleStyle() } }

tasks.jacocoTestReport { reports { xml.required.set(true) } }