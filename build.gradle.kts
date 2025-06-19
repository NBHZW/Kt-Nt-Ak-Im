plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.zealsinger.ktim"
version = "1.0-SNAPSHOT"
val akkaVersion = "2.9.0" // 修正为真实存在的版本
val scalaBinary = "2.13"   // Scala 二进制版本

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.akka.io/maven")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/io.netty/netty-all
    implementation("io.netty:netty-all:4.2.0.Final")
    // Akka 核心依赖
    implementation(platform("com.typesafe.akka:akka-bom_$scalaBinary:$akkaVersion"))
    implementation("com.typesafe.akka:akka-actor_$scalaBinary")
    implementation("com.typesafe.akka:akka-actor-typed_$scalaBinary")
    // implementation("com.typesafe.akka:akka-cluster-typed_$scalaBinary")
    // implementation("com.typesafe.akka:akka-cluster-sharding-typed_$scalaBinary")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}