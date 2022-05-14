import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-noarg:1.4.30")
    }
}

plugins {
    id("maven-publish")
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.32"
    kotlin("plugin.spring") version "1.5.32"
    id("jacoco")
    kotlin("plugin.jpa") version "1.5.32"
}

group = "com.stefanbratanov"
version = "1.3.0"
java.sourceCompatibility = JavaVersion.VERSION_15

jacoco {
    toolVersion = "0.8.7"
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("bootJava") {
            artifact(tasks.getByName("bootJar"))
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/stefanbratanov/sofia-supermarkets-api")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.data:spring-data-keyvalue")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("commons-validator:commons-validator:1.7")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("commons-io:commons-io:2.8.0")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.apache.pdfbox:pdfbox:2.0.22")
    implementation("me.xdrop:fuzzywuzzy:1.3.1")
    implementation("com.cloudinary:cloudinary-http44:1.29.0")
    implementation("org.seleniumhq.selenium:selenium-java:3.141.59")
    implementation("com.codeborne:phantomjsdriver:1.4.4")
    implementation("io.github.bonigarcia:webdrivermanager:4.4.3")
    implementation("com.google.guava:guava:30.1.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23.1")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.mock-server:mockserver-spring-test-listener:5.11.2")
    testRuntimeOnly("com.h2database:h2")
}

springBoot {
    buildInfo()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict","-Xjvm-default=enable")
        jvmTarget = "15"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}
