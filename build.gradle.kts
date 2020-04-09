import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.InetAddress
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

plugins {
    kotlin("jvm") version "1.3.71"

    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.2.6.RELEASE"

    // Makes classes annotated with @Component, @Async, @Transactional, @Cacheable and @SpringBootTest open
    kotlin("plugin.spring") version "1.3.71"

    // Adds a no-arg (Java) constructor to classes annotated with @Entity, @Embeddable or @MappedSuperclass
    kotlin("plugin.jpa") version "1.3.71"

    id("com.github.ben-manes.versions") version "0.28.0"
    id("org.owasp.dependencycheck") version "5.3.2.1"
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "uk.gov.justice.digital.hmpps"

val today: Instant = Instant.now()
val todaysDate: String = LocalDate.now().format(ISO_DATE)

version = if (System.getenv().containsKey("CI")) {
    "${todaysDate}.${System.getenv("CIRCLE_BUILD_NUM")}"
} else {
    todaysDate
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

configurations {

    implementation {
        exclude(mapOf("module" to "tomcat-jdbc"))
    }
}

dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("com.h2database:h2:1.4.200")
    runtimeOnly("org.flywaydb:flyway-core:6.3.3")
    runtimeOnly("org.postgresql:postgresql:42.2.12")

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
    implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.0")
    implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.0")
    implementation("com.github.timpeeters:spring-boot-graceful-shutdown:2.2.1")
    implementation("io.springfox:springfox-swagger2:2.9.2")
    implementation("io.springfox:springfox-swagger-ui:2.9.2")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("net.sf.ehcache:ehcache:2.10.6")
    implementation("org.apache.commons:commons-lang3:3.10")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.3")
    implementation( "com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("com.pauldijou:jwt-core_2.11:4.3.0")
    implementation("com.google.guava:guava:28.2-jre")

    testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.17.0")
    testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
    testImplementation("com.ninja-squad:springmockk:2.0.0")
}

val agentDeps by configurations.register("agentDeps") {
    dependencies {
        "agentDeps"("com.microsoft.azure:applicationinsights-agent:2.6.0") {
            isTransitive = false
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencyCheck {
    failBuildOnCVSS = 5f
    suppressionFiles = listOf("dependency-check-suppress-spring.xml")
    format = Format.ALL
    analyzers.assemblyEnabled = false
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
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
