plugins {
    java
    id("com.diffplug.spotless") version "8.10.2"
}

repositories {
    // fidorial-api built locally with ./gradlew :fidorial-api:publishToMavenLocal
    mavenLocal()
    maven("https://repo.euphyllia.moe/repository/maven-public/")
    mavenCentral()
    // the dependencies of fidorial-api
    maven("https://libraries.minecraft.net")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/maven-snapshots")
}

dependencies {
    // brings Adventure, Brigadier and JSpecify along
    compileOnly("fr.fidorial:fidorial-api:${providers.gradleProperty("fidorialApiVersion").get()}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = Charsets.UTF_8.name()
    options.release = 25
    // the plugin and the API both read libraries that are not modules
    options.compilerArgs.addAll(
        listOf(
            "--add-reads",
            "fr.fidorial.doom=ALL-UNNAMED",
            "--add-reads",
            "fr.fidorial=ALL-UNNAMED",
        ),
    )
}

tasks.withType<ProcessResources>().configureEach {
    filteringCharset = Charsets.UTF_8.name()
}

tasks.jar {
    archiveBaseName.set("DoomPlugin")
    archiveVersion.set("")
}

// ./gradlew deploy -PfidorialServerDir=<folder of the server>: copies the plugin into its plugins folder
val deploy =
    tasks.register<Copy>("deploy") {
        description = "Copies the plugin into the plugins folder of a Fidorial server"
        group = "distribution"
        val serverDir = providers.gradleProperty("fidorialServerDir")
        from(tasks.jar)
        into(serverDir.orElse(layout.buildDirectory.dir("no-server").map { it.asFile.path }).map { "$it/plugins" })
        doFirst {
            if (!serverDir.isPresent) {
                throw GradleException("Give the server folder with -PfidorialServerDir=<folder>")
            }
        }
    }

spotless {
    format("misc") {
        target("*.md", "*.properties", "*.json", "*.yml", "*.yaml")
        targetExclude(".gradle/**", ".idea/**", "**/build/**")
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        ktlint("1.8.0")
    }

    java {
        target("src/**/*.java")
        forbidWildcardImports()
        formatAnnotations()
        removeUnusedImports()
        replaceRegex("Expand empty code blocks", """(?m)^([ \t]*)(.+) \{\}$""", "$1$2 {\n$1}")
        replaceRegex(
            "Separate module requires static",
            """(?m)^(    requires (?!static )[^\n]+;\n)(    requires static )""",
            "$1\n$2",
        )
        importOrder("", "javax|java", "\\#")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
