plugins {
    id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.4.8"
    kotlin("plugin.spring") version "1.3.72"
    kotlin("plugin.jpa") version "1.3.72"
}

configurations {
    implementation { exclude(mapOf("module" to "tomcat-jdbc")) }
}

dependencies {

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("com.h2database:h2:1.4.200")
    runtimeOnly("org.flywaydb:flyway-core:6.5.4")
    runtimeOnly("org.postgresql:postgresql:42.2.15")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("io.springfox:springfox-boot-starter:3.0.0")
    implementation("net.sf.ehcache:ehcache:2.10.6")
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2")
    implementation( "com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("com.nimbusds:nimbus-jose-jwt:8.20")
    implementation("com.google.guava:guava:29.0-jre")

    testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.java:junit-dataprovider:1.13.1")
    testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")
    testImplementation("com.ninja-squad:springmockk:2.0.3")
    testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}