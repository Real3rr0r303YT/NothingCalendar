plugins {
    id("com.android.application")
    kotlin("android")
}

val gdxVersion: String by rootProject.extra
val natives: Configuration by configurations.creating

// Release-Keystore (optional): KEYSTORE_FILE + KEYSTORE_PASS + KEY_ALIAS + KEY_PASS
// Fehlt er, wird der Release-Build mit dem Debug-Key signiert -> APK ist trotzdem installierbar.
val releaseKeystore: File? = System.getenv("KEYSTORE_FILE")
    ?.takeIf { it.isNotBlank() }
    ?.let { file(it) }
    ?.takeIf { it.exists() }

android {
    namespace = "dev.corebreach"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.corebreach"
        minSdk = 26
        targetSdk = 35
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = System.getenv("GITHUB_REF_NAME")
            ?.takeIf { it.startsWith("v") }
            ?.removePrefix("v")
            ?: "0.1.0-dev"
    }

    sourceSets["main"].apply {
        manifest.srcFile("AndroidManifest.xml")
        java.srcDirs("src/main/kotlin")
        res.srcDirs("res")
        assets.srcDirs("../assets")
        jniLibs.srcDirs("libs")
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = System.getenv("KEYSTORE_PASS")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASS")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = if (releaseKeystore != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        jniLibs.useLegacyPackaging = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")

    listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64").forEach { abi ->
        natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-$abi")
        natives("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-$abi")
    }
}

// libGDX-Natives (.so) aus den natives-*.jar nach android/libs/<abi>/ entpacken
val copyAndroidNatives by tasks.registering {
    val nativeJars = natives
    val libsDir = layout.projectDirectory.dir("libs")
    inputs.files(nativeJars)
    outputs.dir(libsDir)
    doLast {
        nativeJars.files.forEach { jar ->
            val abi = jar.name.substringAfter("natives-").removeSuffix(".jar")
            val outDir = libsDir.dir(abi).asFile.apply { mkdirs() }
            project.copy {
                from(project.zipTree(jar))
                into(outDir)
                include("*.so")
            }
        }
    }
}

tasks.matching { it.name.contains("merge") && it.name.contains("JniLibFolders") }
    .configureEach { dependsOn(copyAndroidNatives) }
