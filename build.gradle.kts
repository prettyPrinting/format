plugins {
    kotlin("jvm") version "2.1.0"
}

defaultTasks("test")

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")
    testImplementation("junit:junit:4.13.2")
}

sourceSets {
    main {
        kotlin {
            srcDir("src")
        }
    }
    test {
        kotlin {
            srcDir("tests")
        }
    }
}

tasks.jar {
    from(sourceSets.main.get().allSource)
}

tasks.test {
    finalizedBy(tasks.jar)
}

tasks.wrapper {
    gradleVersion = "8.10"
}
