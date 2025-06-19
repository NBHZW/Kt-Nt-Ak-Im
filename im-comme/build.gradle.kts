plugins {
    kotlin("jvm")
}

group = "com.zealsinger.ktim"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.netty.all)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}