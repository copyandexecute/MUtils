plugins {
    `kotlin-script`
    `paper-script`
    `shadow-script`
}

dependencies {
    implementation(project(":vanilla")) // Main Utils
    implementation(project(":core-paper")) // Paper specified Utils
    implementation(project(":kpaper-light"))

    implementation(project(":challenges:api")) // Internal API
    implementation(project(":bridge:api")) // Bridge
}

group = "de.miraculixx.timer"
setProperty("module_name", "timer")
