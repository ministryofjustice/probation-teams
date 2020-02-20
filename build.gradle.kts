import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.InetAddress
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE


plugins {
    kotlin("jvm") version "1.3.61"

    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.2.4.RELEASE"

    // Makes classes annotated with @Component, @Async, @Transactional, @Cacheable and @SpringBootTest open
    kotlin("plugin.spring") version "1.3.61"

    // Adds a no-arg (Java) constructor to classes annotated with @Entity, @Embeddable or @MappedSuperclass
    kotlin("plugin.jpa") version "1.3.61"

    id("com.github.ben-manes.versions") version "0.27.0"
    id("org.owasp.dependencycheck") version "5.3.0"
}

group = "uk.gov.justice.digital.hmpps"

val today: Instant = Instant.now()
val todaysDate: String = LocalDate.now().format(ISO_DATE)

version = if (System.getenv().containsKey("CI")) {
    "${todaysDate}.${System.getenv("CIRCLE_BUILD_NUM")}"
} else {
    todaysDate
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("com.h2database:h2:1.4.200")
    runtimeOnly("org.flywaydb:flyway-core:6.2.3")
    runtimeOnly("org.postgresql:postgresql:42.2.10")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.hibernate:hibernate-java8")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security.oauth:spring-security-oauth2:2.4.0.RELEASE")
    implementation("org.springframework.security:spring-security-jwt:1.1.0.RELEASE")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3")
    implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.5.1")
    implementation("com.microsoft.azure:applicationinsights-logging-logback:2.5.1")
    implementation("com.github.timpeeters:spring-boot-graceful-shutdown:2.2.0")
    implementation("io.springfox:springfox-swagger2:2.9.2")
    implementation("io.springfox:springfox-swagger-ui:2.9.2")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("net.sf.ehcache:ehcache:2.10.6")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.pauldijou:jwt-core_2.11:4.2.0")

    testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.13.0")
    testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
    testImplementation("com.ninja-squad:springmockk:2.0.0")
}

val agentDeps by configurations.register("agentDeps") {
    dependencies {
        "agentDeps"("com.microsoft.azure:applicationinsights-agent:2.5.1") {
            isTransitive = false
        }
    }
}

configurations {

    implementation {
        exclude(mapOf("module" to "tomcat-jdbc"))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {

    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    test {
        useJUnitPlatform()
    }

    val copyAgent by registering(Copy::class) {
        from(agentDeps)
        into("$buildDir/libs")
    }

    assemble {
        dependsOn(copyAgent)
    }

    bootJar {
        manifest {
            attributes["Implementation-Title"] = rootProject.name
            attributes["Implementation-Version"] = archiveVersion
        }
    }
}

springBoot {
    buildInfo {
        properties {
            artifact = rootProject.name
            version = version
            group = group
            name = rootProject.name
            time = today

            additional = mapOf(
                    "by" to System.getProperty("user.name"),
                    "operatingSystem" to "${System.getProperty("os.name")} (${System.getProperty("os.version")})",
                    "continuousIntegration" to System.getenv().containsKey("CI"),
                    "machine" to InetAddress.getLocalHost().hostName
            )
        }
    }
}
