import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

fun getLocalProperty(key: String): String? {
    return gradleLocalProperties(rootDir).getProperty(key)
}

fun String?.toFile() = file(this!!)

val environment: Map<String, String> = System.getenv()

android {
    namespace = "com.app.musicapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.musicapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("MyAppSigningConfig") {
            keyAlias = getLocalProperty("signing.keyAlias") ?: environment["SIGNING_KEY_ALIAS"] ?: error("Missing keyAlias")
            keyPassword = getLocalProperty("signing.keyPassword") ?: environment["SIGNING_KEY_PASSWORD"] ?: error("Missing keyPassword")
            storeFile = (getLocalProperty("signing.storeFile") ?: environment["SIGNING_STORE_FILE"] ?: error("Missing storeFile")).toFile()
            storePassword = getLocalProperty("signing.storePassword") ?: environment["SIGNING_STORE_PASSWORD"] ?: error("Missing storePassword")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs["MyAppSigningConfig"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")

    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}