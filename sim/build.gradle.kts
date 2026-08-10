plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("jp.aoto.zerosum.sim.MainKt")
}

