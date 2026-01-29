import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.compose") version "1.7.0"
    id("com.android.library")
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.skie)
    alias(libs.plugins.moko.multiplatform.resources)
    id("org.jetbrains.kotlin.plugin.parcelize")
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)

}

// Version management
val sharedVersion = project.findProperty("dadomatch.shared.version")?.toString()
    ?: System.getenv("SHARED_VERSION")
    ?: "1.0.0"

version = sharedVersion
group = "com.dadomatch.shared"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    val xcf = XCFramework("DadoMatchShared")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "DadoMatchShared"
            binaryOption("bundleId", "com.dadomatch.shared")
            binaryOption("bundleVersion", sharedVersion)
            binaryOption("bundleShortVersionString", sharedVersion)
            freeCompilerArgs += listOf("-Xoverride-konan-properties=minVersion.ios=16.0;minVersionSinceXcode15.ios=16.0")
            xcf.add(this)
            isStatic = true
            transitiveExport = true
            export(libs.moko.resources)
            export(libs.moko.graphics)
            export(libs.compose.animation)
        }
    }

    sourceSets {
        androidMain {
            kotlin.srcDir("build/generated/moko/androidMain/src")
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(compose.preview)
                implementation(compose.components.uiToolingPreview)
                api(libs.koin.android)
                api(libs.koin.androidx.compose)

            }
        }
        commonMain {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.material3)
                implementation(libs.material.icons.core)
                implementation(compose.runtime)
                implementation(compose.components.resources)
                implementation(libs.compose.view.model)
                //api(compose.animation)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentnegotiation)
                implementation(libs.ktor.client.serialization.json)
                api(libs.moko.resources)
                api(libs.moko.graphics)
                api(libs.moko.resources.compose) // for compose multiplatform
                api(libs.datastore.preferences)
                api(libs.datastore)

                api(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.lifecycle.viewmodel)
                api(libs.compose.webview.multiplatform)
                implementation(libs.coil3.compose)
                implementation(libs.coil3.network.ktor)
                api(libs.androidx.navigation.compose)
                implementation(libs.kotlinx.datetime)
                implementation(libs.calf.file.picker)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

android {
    namespace = "com.dadomatch.shared"
    compileSdk = 35
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 24
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        baseline = file("lint-baseline.xml")
    }

}

multiplatformResources {
    resourcesPackage.set("com.dadomatch.shared.resources")
}

skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}

ktlint {
    version.set("0.50.0")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(true)
    enableExperimentalRules.set(false)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        exclude("**/MR.kt")
        include("**/kotlin/**")
    }
}

dependencies {
    add("debugImplementation", libs.compose.ui.tooling)
}
