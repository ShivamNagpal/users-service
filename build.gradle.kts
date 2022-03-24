import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.nagpal.shivam"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val vertxVersion = "4.1.2"
val junitJupiterVersion = "5.7.2"
val lombokVersion = "1.18.20"

val launcherClassName = "com.nagpal.shivam.workout_manager_user.Main"

val doOnChange = "${projectDir}/gradlew classes"

application {
    mainClass.set(launcherClassName)
}

dependencies {
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-web-validation:$vertxVersion")
    implementation("io.vertx:vertx-pg-client:$vertxVersion")
    implementation("io.vertx:vertx-mongo-client:$vertxVersion")
    implementation("io.vertx:vertx-mail-client:$vertxVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    // https://mvnrepository.com/artifact/org.flywaydb/flyway-core
    implementation("org.flywaydb:flyway-core:7.14.1")
    // Note: Blocking Driver, so included only for Flyway to work
    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    implementation("org.postgresql:postgresql:42.2.23")
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-crypto
    implementation("org.springframework.security:spring-security-crypto:5.5.2")
    // https://mvnrepository.com/artifact/com.auth0/java-jwt
    implementation("com.auth0:java-jwt:3.18.1")

    testImplementation("io.vertx:vertx-junit5:$vertxVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
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
