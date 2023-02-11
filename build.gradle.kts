import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    //id("org.springframework.experimental.aot") version "0.11.5" //(LOGGING BUG)

    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}

group = "pe.proxy"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_18

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.register("runDev") {
    group = "application"
    description = "Runs the Spring Boot application with the dev profile"
    doFirst {
        tasks.bootRun.configure {
            systemProperty("spring.profiles.active", "dev")
        }
    }
    finalizedBy("bootRun")
}

repositories {
    mavenCentral()
    //maven { url = uri("https://repo.spring.io/release") }
    //maven { url = uri("https://repo.spring.io/milestone") }
    //maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    //Jackson Modules for serialization/deserialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformats-text:2.14.2")

    //Apache CSV
    implementation("org.apache.commons:commons-csv:1.10.0")

    //Netty4 - Connecting to Endpoint Test Server, Testing the Proxy
    implementation("io.netty:netty-handler-proxy:4.1.87.Final")
    //implementation("io.netty:netty-all:4.1.78.Final")

    //JPA API - Interacting with JDBC
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    //Spring Web - Minor API for checking if Application is running
    implementation("org.springframework.boot:spring-boot-starter-web")

    //Twilio API (Sending alerts if detects one of the endpoint servers are down)
    //TODO - Switch this to just a simple REST request, library isn't needed for EndpointMonitor
    implementation("com.twilio.sdk:twilio:9.2.3")

    //Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.3")

    //WebFlux & Reactor - Minor API for checking if Application is running
    //implementation("org.springframework.boot:spring-boot-starter-webflux")
    //implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    //Spring Security - Allow access to specific directory's
    implementation("org.springframework.boot:spring-boot-starter-security")

    //Kotlin Defaults
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
    //implementation("org.junit.jupiter:junit-jupiter:5.8.2")

    //KotlinX - Serializing JSON data to Kotlin Class
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")

    //Jsoup to sanitize html
    //implementation("org.jsoup:jsoup:1.15.1")

    //Unit Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test:3.4.24")

    //testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    //testImplementation("org.mockito:mockito-core:4.5.1")
    //testImplementation("org.hamcrest:hamcrest:2.2")

    //Configuration Processor - For YAML configs
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    //Devtools - Live debugging
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    //MariaDB - SQL Database for storing the tested Proxies
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.1.2")
    //Reflection for Kotlin
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "18"
        apiVersion = "1.8"
        languageVersion = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/*
tasks.withType<BootBuildImage> {
    builder = "paketobuildpacks/builder:tiny"
    environment = mapOf("BP_NATIVE_IMAGE" to "true")
}*/
