plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("com.google.gms.google-services")
}
android { namespace="tw.sometools.pogocollector"; compileSdk=35
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
 defaultConfig { applicationId="tw.sometools.pogocollector"; minSdk=26; targetSdk=35; versionCode=2; versionName="1.1.0" }
}
dependencies {
 implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
 implementation("com.google.firebase:firebase-auth")
 implementation("com.google.firebase:firebase-firestore")
}
