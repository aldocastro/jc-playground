plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.overview"
    compileSdk = 36
    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    dependencies {
        implementation("androidx.core:core-ktx:1.16.0")
        implementation("androidx.appcompat:appcompat:1.7.1")
        implementation("androidx.activity:activity-compose:1.10.1")
        implementation("androidx.compose.foundation:foundation:1.8.3")
        implementation("androidx.compose.runtime:runtime:1.8.3")
        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
        implementation("androidx.compose.material3:material3:1.3.2")
        implementation("androidx.compose.ui:ui-tooling-preview-android:1.8.3")
        implementation("io.coil-kt.coil3:coil-compose:3.3.0")
        implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
        implementation(platform("androidx.compose:compose-bom:2024.09.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation ("androidx.compose.ui:ui-tooling-preview")
        implementation ("androidx.compose.foundation:foundation:1.8.3")

        testImplementation("junit:junit:4.+")
        androidTestImplementation ("androidx.test.ext:junit:1.1.2")
        androidTestImplementation ("androidx.test.espresso:espresso-core:3.3.0")

        androidTestImplementation ("androidx.compose.ui:ui-test-junit4")
        debugImplementation ("androidx.compose.ui:ui-tooling")
        debugImplementation ("androidx.compose.ui:ui-test-manifest")
    }
}
