plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.4"

  kotlin("plugin.spring") version "1.5.20"
  kotlin("plugin.jpa") version "1.5.20"
}

configurations {
  implementation { exclude(group = "tomcat-jdbc") }
}

dependencies {

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("io.springfox:springfox-boot-starter:3.0.0")
  implementation("net.sf.ehcache:ehcache")
  implementation("org.apache.commons:commons-lang3")

  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.ninja-squad:springmockk:3.0.1")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable"
  )
}
