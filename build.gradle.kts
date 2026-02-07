plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    group = "com.dadomatch.shared"
    version = "1.0.0"
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory.asFile.get())
}
