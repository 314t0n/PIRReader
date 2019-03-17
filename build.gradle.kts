import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.gradle.jvm.tasks.Jar
import kotlin.collections.set

group = "space.sentinel"
version = "1.0-SNAPSHOT"

plugins {
    application
    kotlin("jvm") version "1.3.20"
    id("de.undercouch.download").version("3.4.3")
}

application {
    mainClassName = "space.hajnal.App"
}

dependencies {
    compile(kotlin("stdlib"))
//    compile(files("pi4j-jdk11-release~1.1-ge053148-137.jar"))
    compile("com.pi4j:pi4j-core:1.1")
    compile("io.projectreactor:reactor-core:3.2.6.RELEASE")
    compile("org.slf4j:slf4j-api:1.7.26")
    compile("ch.qos.logback:logback-classic:0.9.26")
    compile("ch.qos.logback:logback-core:0.9.26")
    compile("javax.xml.bind:jaxb-api:2.3.0")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.4.0")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
}

repositories {
    jcenter()
}

val fatJar = task("fatJar", type = Jar::class) {
    manifest {
        attributes["Implementation-Title"] = "PIR Sensor Reader"
        attributes["Implementation-Version"] = "1.0"
        attributes["Main-Class"] = "space.hajnal.App"
    }
    from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
    task<Download>("download-pi4j") {
        src("https://jitpack.io/com/github/Robo4J/pi4j/jdk11-release~1.1-ge053148-137/pi4j-jdk11-release~1.1-ge053148-137.jar")
        dest("${buildDir}/libs/PIR")
        onlyIfModified(true)
    }
}

//defaultTasks("download-pi4j")