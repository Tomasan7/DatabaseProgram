import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

group = "me.tomasan7"
version = "0.0.4"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.core.jvm)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.exposed.core)
    runtimeOnly(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    runtimeOnly(libs.h2)
    runtimeOnly(libs.mssql.jdbc)
    implementation(libs.hoplite.core)
    implementation(libs.hoplite.hocon)
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)

    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.compose.previewer)
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.screenModel)
    //implementation(libs.humanReadable)
    implementation(libs.compose.file.picker)

    //implementation(libs.diglolCrypto.kdf)
    implementation(libs.diglolCrypto.hash)

    implementation(libs.kotlinCsv.jvm)
}

kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "me.tomasan7.opinet.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "OpiNet"
            packageVersion = version.toString()
        }
    }
}

/* mssql-jdbc includes some stupid signatures, which make the resulting jar unrunnable */
tasks.withType<Jar> {
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}
