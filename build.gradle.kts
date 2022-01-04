val utilsVersion = "0.3.3"
val h2Version = "1.4.200"
val saVersion = "1.28.0"
val queryDslVersion = "5.0.0"
val coroutinesVersion = "1.6.0-native-mt"
val okhttpVersion = "4.9.3"

plugins {
    val kotlinVersion = "1.6.10"
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

group = "me.kuku"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://nexus.kuku.me/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-devtools")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("me.kuku:utils:$utilsVersion")
    implementation("com.h2database:h2")
    implementation("cn.dev33:sa-token-reactor-spring-boot-starter:$saVersion")
    implementation("com.querydsl:querydsl-core:$queryDslVersion")
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jpa")
}