import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    id("com.android.library")
    alias(libs.plugins.kotlinxSerialization)
    id("org.jetbrains.kotlin.plugin.parcelize")
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ksp)
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
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(compose.preview)
                implementation(compose.components.uiToolingPreview)
                api(libs.koin.android)
                api(libs.koin.androidx.compose)
                
                // Native Auth Dependencies
                implementation(libs.credentials)
                implementation(libs.credentials.play.services.auth)
                implementation(libs.googleid)
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
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentnegotiation)
                implementation(libs.ktor.client.serialization.json)

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
                implementation(libs.generative.ai)
                implementation(libs.compottie)
                
                // Room
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
                
                // RevenueCat
                implementation(libs.revenuecat.purchases.core)
                implementation(libs.revenuecat.purchases.result)
                implementation(libs.revenuecat.purchases.ui)

                // Firebase
                implementation(libs.firebase.auth)
                implementation(libs.firebase.common)
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
        
        // Opt-in to ExperimentalForeignApi for RevenueCat iOS bindings
        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
            compilations.all {
                compilerOptions.configure {
                    freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
    }
}

// KSP Configuration
dependencies {
    // Don't add kspCommonMainMetadata - it conflicts with platform-specific actuals
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
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
        include("**/kotlin/**")
    }
}

dependencies {
    add("debugImplementation", libs.compose.ui.tooling)
}

buildkonfig {
    packageName = "com.dadomatch.shared"
    objectName = "BuildKonfig"
    exposeObjectWithName = "BuildKonfig"

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }
    
    // Determine flavor from local.properties, project property, or default to stage
    // Priority: 1. Command line (-Papp.flavor=production)
    //           2. local.properties (app.flavor=production)
    //           3. Default to stage
    val appFlavor = project.findProperty("app.flavor")?.toString() 
        ?: System.getenv("APP_FLAVOR")
        ?: localProperties.getProperty("app.flavor") 
        ?: "stage"
    val isProduction = appFlavor == "production"
    
    println("BuildKonfig: Building for flavor: $appFlavor")
    
    defaultConfigs {
        val geminiApiKey =
            localProperties.getProperty("GEMINI_API_KEY")
                ?: System.getenv("GEMINI_API_KEY")
                ?: ""


        val geminiModelName = localProperties.getProperty("GEMINI_MODEL_NAME")
                ?: System.getenv("GEMINI_MODEL_NAME")
                ?: "gemini-2.5-flash-lite"

        
        val revenueCatApiKey = if (isProduction) {
            localProperties.getProperty("PROD_REVENUECAT_API_KEY") 
                ?: System.getenv("PROD_REVENUECAT_API_KEY") 
                ?: ""
        } else {
            localProperties.getProperty("STAGE_REVENUECAT_API_KEY") 
                ?: System.getenv("STAGE_REVENUECAT_API_KEY") 
                ?: ""
        }
        
        val apiBaseUrl = if (isProduction) {
            localProperties.getProperty("PROD_API_BASE_URL")
                ?: System.getenv("PROD_API_BASE_URL")
                ?: "https://api.dadomatch.com"
        } else {
            localProperties.getProperty("STAGE_API_BASE_URL")
                ?: System.getenv("STAGE_API_BASE_URL")
                ?: "https://api-stage.dadomatch.com"
        }
        
        val googleWebClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID")
                ?: System.getenv("GOOGLE_WEB_CLIENT_ID")
                ?: ""
        
        val environment = if (isProduction) "production" else "stage"
        val isDebug = !isProduction

        buildConfigField(STRING, "GEMINI_API_KEY", geminiApiKey)
        buildConfigField(STRING, "GEMINI_MODEL_NAME", geminiModelName)
        buildConfigField(STRING, "REVENUECAT_API_KEY", revenueCatApiKey)
        buildConfigField(STRING, "API_BASE_URL", apiBaseUrl)
        buildConfigField(STRING, "GOOGLE_WEB_CLIENT_ID", googleWebClientId)
        buildConfigField(STRING, "ENVIRONMENT", environment)
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN, "IS_DEBUG", isDebug.toString())
    }
}



