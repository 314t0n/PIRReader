group = "space.sentinel"
version = "1.0-SNAPSHOT"

plugins {
    application
    kotlin("jvm") version "1.3.20"
}

application {
    mainClassName = "space.hajnal.App"
}

dependencies {
    compile(kotlin("stdlib"))
}

repositories {
    jcenter()
}