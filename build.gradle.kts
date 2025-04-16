import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.6"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
}

group = "se.wilmer"
version = "1.0.0-SNAPSHOT"
description = "Something"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
    implementation("org.spongepowered:configurate-gson:4.2.0-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.2.0-SNAPSHOT")
    implementation("com.jeff-media:custom-block-data:2.2.4")
    implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    shadowJar {
        relocate("com.jeff_media.morepersistentdatatypes", "se.wilmer.morepersistentdatatypes")
        relocate("com.jeff_media.customblockdata", "se.wilmer.customblockdata")
    }
}

bukkitPluginYaml {
    main = "se.wilmer.factory.Factory"
    apiVersion = "1.21.4"
}
