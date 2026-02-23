version = "0.0.1-SNAPSHOT"

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.dom4j)
    implementation(libs.jaxen)
}

tasks.test {
    useJUnitPlatform()
}