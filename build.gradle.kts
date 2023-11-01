import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.nagpal.shivam"
version = "1.0.0"

repositories {
    mavenCentral()
}

object Versions {
    const val FLYWAY = "9.22.3"
    const val JACKSON = "2.15.3"
    const val JAVA_JWT = "3.19.4"
    const val JUNIT_JUPITER = "5.9.1"
    const val LOMBOK = "1.18.30"
    const val POSTGRESQL = "42.6.0"
    const val SCRAM_CLIENT = "2.1"
    const val SPRING_SECURITY = "5.8.8"
    const val VERTX = "4.4.6"
}

val launcherClassName = "dev.shivamnagpal.users.Main"

val doOnChange = "${projectDir}/gradlew classes"

application {
    mainClass.set(launcherClassName)
}

dependencies {
    implementation(platform("io.vertx:vertx-stack-depchain:${Versions.VERTX}"))
    implementation("io.vertx:vertx-config:${Versions.VERTX}")
    implementation("io.vertx:vertx-web-validation:${Versions.VERTX}")
    implementation("io.vertx:vertx-pg-client:${Versions.VERTX}")
    implementation("io.vertx:vertx-mongo-client:${Versions.VERTX}")
    implementation("io.vertx:vertx-mail-client:${Versions.VERTX}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.JACKSON}")
    compileOnly("org.projectlombok:lombok:${Versions.LOMBOK}")
    annotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
    // https://mvnrepository.com/artifact/org.flywaydb/flyway-core
    implementation("org.flywaydb:flyway-core:${Versions.FLYWAY}")
    // Note: Blocking Driver, included only for Flyway to work
    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    implementation("org.postgresql:postgresql:${Versions.POSTGRESQL}")
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-crypto
    implementation("org.springframework.security:spring-security-crypto:${Versions.SPRING_SECURITY}")
    // https://mvnrepository.com/artifact/com.auth0/java-jwt
    implementation("com.auth0:java-jwt:${Versions.JAVA_JWT}")
    // https://mvnrepository.com/artifact/com.ongres.scram/client
    implementation("com.ongres.scram:client:${Versions.SCRAM_CLIENT}")

    testImplementation("io.vertx:vertx-junit5:${Versions.VERTX}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.JUNIT_JUPITER}")
    testCompileOnly("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
    mergeServiceFiles()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
    }
}
