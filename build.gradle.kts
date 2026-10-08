import gg.essential.gradle.util.*

plugins {
    id("gg.essential.multi-version")
    id("gg.essential.defaults.repo")
    id("gg.essential.defaults.java")
    id("gg.essential.defaults.loom")
}

// Force Java 21 for all versions (only JDK available on this machine)
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

loom {
    noRunConfigs()
}

if (!platform.isUnobfuscated) {
    loom.mixin.useLegacyMixinAp = true
    loom.mixin.defaultRefmapName.set("mixins.jgui.refmap.json")
}

java.withSourcesJar()

repositories {
    mavenLocal()
    maven("https://jitpack.io") {
        content {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
    maven("https://repo.spongepowered.org/maven/") // for 0.7.11-SNAPSHOT Mixin
}

dependencies {
    // NeoForge adds InGameHud.renderHotbar, clashing with Yarn's name for the vanilla method; these versions
    // layer a Tiny file over Yarn to rename the vanilla one (Yarn is therefore not set via gradle.properties).
    val yarnWithNeoForgeFixes = mapOf(
        "1.21.8-neoforge" to "net.fabricmc:yarn:1.21.8+build.1:v2",
        "1.21.10-neoforge" to "net.fabricmc:yarn:1.21.10+build.2:v2",
    )[project.name]
    if (yarnWithNeoForgeFixes != null) {
        mappings(loom.layered {
            mappings(yarnWithNeoForgeFixes)
            mappings(file("neoforge-mapping-fixes.tiny"))
        })
    }

    api("com.github.ReplayMod:lwjgl-utils:27dcd66")

    if (!platform.isFabric) {
        // Mixin 0.8 is no longer compatible with MC 1.11.2 or older
        val mixinVersion = if (platform.mcVersion >= 11200) "0.8.2" else "0.7.11-SNAPSHOT"
        compileOnly("org.spongepowered:mixin:$mixinVersion")
    }

    if (platform.mcVersion >= 11604) {
        implementation(annotationProcessor("com.github.LlamaLad7:MixinExtras:0.1.1")!!)
    }
}
