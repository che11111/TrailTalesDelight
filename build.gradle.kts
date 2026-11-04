plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "dev.che1s"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
}

val ceVersion = providers.gradleProperty("ceVersion").getOrElse("26.9.1")

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("net.momirealms:craft-engine-bukkit:$ceVersion")
    compileOnly("net.momirealms:craft-engine-core:$ceVersion")
    compileOnly("net.momirealms:craft-engine-bukkit-proxy:$ceVersion")
    compileOnly("com.huidu.farmersdelight:farmersdelight-plugin:1.0.3")
    implementation("org.bstats:bstats-bukkit:3.2.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.yaml:snakeyaml:2.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching(listOf(
        "paper-plugin.yml",
        "craftengine/trailandtales_delight/pack.yml"
    )) {
        expand("version" to version)
    }
    from("LICENSE") {
        into("META-INF")
    }
}

tasks.shadowJar {
    archiveBaseName.set("TrailTalesDelight")
    archiveClassifier.set("")
    relocate("org.bstats", "dev.che1s.trailtalesdelight.bstats")
}

configurations.testImplementation {
    extendsFrom(configurations.compileOnly.get())
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
