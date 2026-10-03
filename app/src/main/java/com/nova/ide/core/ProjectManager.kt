package com.nova.ide.core

import android.content.Context
import java.io.File

class ProjectManager(context: Context) {
    private val workspace = File(context.filesDir, "nova-projects")
    init { check(workspace.exists() || workspace.mkdirs()) { "Cannot create workspace" } }

    fun getProjects(): List<File> =
        workspace.listFiles()?.filter { it.isDirectory }?.sortedBy { it.name } ?: emptyList()

    fun createAndroidProject(name: String): File {
        require(name.matches(Regex("[A-Za-z][A-Za-z0-9_]*"))) { "Use a valid name such as MyApp" }
        val pkg = "com.example.${name.lowercase()}"
        val dir = File(workspace, name)
        require(!dir.exists()) { "Project already exists" }
        check(dir.mkdirs()) { "Cannot create project folder" }
        try {
            put(dir, "settings.gradle.kts", """
                pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
                dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
                rootProject.name = "$name"
                include(":app")
            """)
            put(dir, "build.gradle.kts", """
                plugins {
                    id("com.android.application") version "8.7.3" apply false
                    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
                }
            """)
            put(dir, "gradle.properties", "org.gradle.jvmargs=-Xmx2048m\nandroid.useAndroidX=true")
            put(dir, "app/build.gradle.kts", """
                plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
                android {
                    namespace = "$pkg"
                    compileSdk = 35
                    defaultConfig {
                        applicationId = "$pkg"
                        minSdk = 26
                        targetSdk = 35
                        versionCode = 1
                        versionName = "1.0"
                    }
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_17
                        targetCompatibility = JavaVersion.VERSION_17
                    }
                    kotlinOptions { jvmTarget = "17" }
                }
                dependencies {
                    implementation("androidx.core:core-ktx:1.15.0")
                    implementation("androidx.appcompat:appcompat:1.7.0")
                    implementation("com.google.android.material:material:1.12.0")
                }
            """)
            put(dir, "app/src/main/AndroidManifest.xml", """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                    <application android:theme="@style/Theme.Starter" android:label="$name">
                        <activity android:name=".MainActivity" android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN"/>
                                <category android:name="android.intent.category.LAUNCHER"/>
                            </intent-filter>
                        </activity>
                    </application>
                </manifest>
            """)
            put(dir, "app/src/main/java/${pkg.replace('.', '/')}/MainActivity.kt", """
                package $pkg
                import android.os.Bundle
                import androidx.appcompat.app.AppCompatActivity
                class MainActivity : AppCompatActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.activity_main)
                    }
                }
            """)
            put(dir, "app/src/main/res/layout/activity_main.xml", """
                <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent" android:layout_height="match_parent"
                    android:gravity="center" android:orientation="vertical">
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                        android:text="Hello from $name" android:textSize="24sp"/>
                </LinearLayout>
            """)
            put(dir, "app/src/main/res/values/styles.xml", """
                <resources>
                    <style name="Theme.Starter" parent="Theme.MaterialComponents.DayNight.NoActionBar"/>
                </resources>
            """)
            put(dir, "README.md", "# $name\nCreated by NOVA IDE.")
            return dir
        } catch (e: Exception) {
            dir.deleteRecursively()
            throw e
        }
    }

    private fun put(base: File, relative: String, body: String) {
        val target = File(base, relative)
        check(target.canonicalPath.startsWith(base.canonicalPath + File.separator)) { "Invalid path" }
        target.parentFile?.mkdirs()
        target.writeText(body.trimIndent() + "\n")
    }
}
