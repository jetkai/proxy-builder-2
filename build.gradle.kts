import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    //id("org.springframework.experimental.aot") version "0.11.5" //(LOGGING BUG)

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
}

group = "pe.proxy"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

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
    maven { url = uri("https://repo.spring.io/release") }
    mavenCentral()
}

dependencies {
    //KotlinX - Serializing JSON data to Kotlin Class
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    //Jackson Module for Serialization to Kotlin Classes
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    //Netty4 - Connecting to Endpoint Test Server, Testing the Proxy
    implementation("io.netty:netty-all:4.1.77.Final")

    //JPA API - Interacting with JDBC
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    //Spring Web - Minor API for checking if Application is running
    implementation("org.springframework.boot:spring-boot-starter-web")

    //WebFlux & Reactor - Minor API for checking if Application is running
    //implementation("org.springframework.boot:spring-boot-starter-webflux")
    //implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    //Spring Security - Allow access to specific directory's
    implementation("org.springframework.boot:spring-boot-starter-security")

    //Kotlin Defaults
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    //Unit Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")

    //Configuration Processor - For YML configs
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    //Devtools - Live debugging
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    //MariaDB - SQL Database for storing the tested Proxies
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.0.4")
    //Reflection for Kotlin
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
        apiVersion = "1.6"
        languageVersion = "1.6"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootBuildImage> {
    builder = "paketobuildpacks/builder:tiny"
    environment = mapOf("BP_NATIVE_IMAGE" to "true")
}
