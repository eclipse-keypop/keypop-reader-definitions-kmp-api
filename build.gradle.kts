///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////

import java.io.DataInputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.dokka)
  alias(libs.plugins.spotless)
  signing
  `maven-publish`
}

// Gradle properties are read through `providers.gradleProperty`: the `by project` delegate is
// deprecated and scheduled for removal in Gradle 10.
fun gradleProperty(name: String): String = providers.gradleProperty(name).get()

val title = gradleProperty("title")
val javaTargetLevel = gradleProperty("javaTargetLevel")
val specApiVersion = gradleProperty("spec.apiVersion")
val specSegment = gradleProperty("spec.segment")
val specDocBaseUrl = gradleProperty("spec.docBaseUrl")

/** Entry point of the specification revision this API transcribes, always its latest patch. */
val specDocUrl = "$specDocBaseUrl/$specSegment/latest-$specApiVersion/"

// The project, SCM and CI URLs are derived from the name of the repository rather than declared:
// they are the five values a module created from this one would silently inherit from it.
val repoUrl = "${gradleProperty("organization.repoBaseUrl")}/${rootProject.name}"
val scmConnection = "scm:git:git://${repoUrl.removePrefix("https://")}.git"
val scmDeveloperConnection = "scm:git:$repoUrl.git"
val ciUrl = "$repoUrl/actions"

val releaseTag: Provider<String> = providers.gradleProperty("releaseTag")

if (releaseTag.isPresent) {
  project.version = releaseTag.get()
  println("Release mode: version set to ${project.version}")
} else {
  println("Development mode: version is ${project.version}")
}

///////////////////////////////////////////////////////////////////////////////
//  KOTLIN MULTIPLATFORM CONFIGURATION
///////////////////////////////////////////////////////////////////////////////

kotlin {
  explicitApi()

  // No toolchain is pinned: Gradle 9 already requires a JDK 17 or later to run, so pinning one
  // would not raise a floor, it would force that exact version -- and Gradle 10 will require 21,
  // which would make a second JDK necessary. What guarantees the Java 8 output is the pair of
  // compiler options below, which `checkJvmBytecode` verifies on every build.

  // No androidTarget: this API carries no resource, manifest nor platform code, so the JVM jar is
  // what an Android consumer resolves, and no AAR has to be published.
  jvm()

  // Apple klibs are cross-compiled from any host since Kotlin 2.2.20, so these targets need no
  // macOS runner: the library has no cinterop dependency, no CocoaPods integration and no final
  // Apple binary to build or test.
  iosArm64()
  iosSimulatorArm64()
  iosX64()

  sourceSets { commonTest.dependencies { implementation(kotlin("test")) } }
}

// Java 8 bytecode, so that the library can be integrated on legacy platforms. `-Xjdk-release`
// closes the newer JDK API to the compiler, which `jvmTarget` alone does not do: together they make
// the output independent of the JDK that runs the build.
tasks.withType<KotlinJvmCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
    freeCompilerArgs.add("-Xjdk-release=$javaTargetLevel")
  }
}

///////////////////////////////////////////////////////////////////////////////
//  VERIFICATION TASKS
///////////////////////////////////////////////////////////////////////////////

/**
 * Verifies that the emitted JVM bytecode really targets Java 8, rather than trusting the
 * configuration: a class file of major version 52 is Java 8.
 */
val checkJvmBytecode: TaskProvider<Task> =
    tasks.register("checkJvmBytecode") {
      description = "Checks that the JVM class files target Java 8."
      group = LifecycleBasePlugin.VERIFICATION_GROUP
      dependsOn("compileKotlinJvm")
      val classesDir: Provider<Directory> = layout.buildDirectory.dir("classes/kotlin/jvm/main")
      val targetLevel = javaTargetLevel
      val expected = JavaVersion.toVersion(targetLevel).majorVersion.toInt() + 44
      val report: Provider<RegularFile> =
          layout.buildDirectory.file("reports/verification/jvm-bytecode.txt")
      val eol: String = System.lineSeparator()
      inputs.dir(classesDir).withPropertyName("classes").skipWhenEmpty()
      outputs.file(report).withPropertyName("report")
      doLast {
        val offenders = mutableListOf<String>()
        var examined = 0
        classesDir
            .get()
            .asFile
            .walkTopDown()
            .filter { it.extension == "class" }
            .forEach { file ->
              DataInputStream(file.inputStream().buffered()).use { input ->
                input.skipBytes(4) // 0xCAFEBABE
                input.readUnsignedShort() // minor version
                val major = input.readUnsignedShort()
                examined++
                if (major != expected) offenders += "${file.name}: major version $major"
              }
            }
        if (offenders.isNotEmpty()) {
          throw GradleException(
              "Expected Java $targetLevel bytecode (major version $expected), found:\n" +
                  offenders.joinToString("\n") { "  - $it" })
        }
        report.get().asFile.apply {
          parentFile.mkdirs()
          writeText(
              "Java $targetLevel bytecode (major version $expected): " +
                  "$examined class files examined, none in violation." +
                  eol)
        }
      }
    }

/**
 * Files whose mentions of the specification version are *derived* from `spec.apiVersion`, and may
 * therefore be rewritten mechanically by `updateSpecVersion`.
 *
 * `CHANGELOG.md` is deliberately absent: its entries state what a past release did, and stay true
 * when the specification moves on. Rewriting them would rewrite history.
 */
val specVersionedFiles: FileCollection =
    files(
        fileTree("src/commonMain/kotlin") { include("**/*.kt") },
        "docs/Module.md",
        "docs/uml/class-diagram.puml",
        "README.md",
        "CONTRIBUTING.md")

/**
 * The three shapes a derived mention of the specification version takes. Each captures the version
 * in its first group, and each is anchored on its context so that no unrelated `1.0` is caught: the
 * alias of a documentation URL, a human-readable mention of the API, and the declaration of the
 * `VERSION` constant -- in the Kotlin source and in the class diagram alike.
 */
val specVersionPatterns =
    listOf(
        Regex("""latest-(\d+\.\d+)"""),
        Regex("""API (\d+\.\d+)"""),
        Regex("""VERSION: String = "(\d+\.\d+)""""))

/** Replaces the captured version of a match by [version], leaving the rest of the match intact. */
fun swapVersion(match: MatchResult, version: String): String {
  val group = match.groups[1]!!.range
  val from = group.first - match.range.first
  val to = group.last - match.range.first + 1
  return match.value.replaceRange(from, to, version)
}

/**
 * Keeps every statement of the specification version consistent, so that the synchronisation rule
 * -- the API and the specification share MAJOR.MINOR, each advancing its own patch -- is a build
 * failure rather than a convention. Three things are verified:
 * 1. every derived mention in [specVersionedFiles] states `spec.apiVersion`,
 * 2. the `VERSION` of the `*ApiProperties` object states it too,
 * 3. every public type links to its own normative definition, the anchor being derivable from its
 *    name,
 * 4. the version of the library shares that major and minor.
 *
 * It only reports; `updateSpecVersion` is the task that rewrites.
 */
val checkSpecConsistency: TaskProvider<Task> =
    tasks.register("checkSpecConsistency") {
      description = "Checks every statement of the specification version against spec.apiVersion."
      group = LifecycleBasePlugin.VERIFICATION_GROUP
      val sources = specVersionedFiles
      val kotlinSources: FileCollection =
          files(fileTree("src/commonMain/kotlin") { include("**/*.kt") })
      val patterns = specVersionPatterns
      val libraryVersion = project.version.toString()
      val expected = specApiVersion
      val expectedUrl = specDocUrl
      val projectDir = layout.projectDirectory.asFile
      val report: Provider<RegularFile> =
          layout.buildDirectory.file("reports/verification/spec-consistency.txt")
      val eol: String = System.lineSeparator()
      inputs.files(sources).withPropertyName("sources")
      inputs.files(kotlinSources).withPropertyName("kotlinSources")
      inputs.property("libraryVersion", libraryVersion)
      inputs.property("specApiVersion", expected)
      outputs.file(report).withPropertyName("report")
      doLast {
        val errors = mutableListOf<String>()
        var mentions = 0

        sources.files
            .filter { it.isFile }
            .sortedBy { it.path }
            .forEach { file ->
              val text = file.readText()
              val name = file.toRelativeString(projectDir).replace('\\', '/')
              patterns.forEach { pattern ->
                pattern.findAll(text).forEach { match ->
                  mentions++
                  val found = match.groupValues[1]
                  if (found != expected) {
                    val line = text.take(match.range.first).count { it == '\n' } + 1
                    errors += "$name:$line states \"${match.value}\", expected version $expected"
                  }
                }
              }
            }

        // Every public type must carry the link to its own normative definition. Checking the
        // version of a link that is there does not catch a type declared without one, nor an
        // anchor copied from a sibling: both pass unnoticed, and both are what transcribing by
        // copy misses. The anchor is derivable -- the specification anchors a type as
        // `[[type_<Name>]]` -- so the expectation is exact.
        var types = 0
        val declaration =
            Regex(
                """(?m)^public\s+(?:object|enum\s+class|annotation\s+class|class|interface)\s+(\w+)""")
        kotlinSources.files
            .filter { it.isFile }
            .sortedBy { it.path }
            .forEach { file ->
              val text = file.readText()
              val name = file.toRelativeString(projectDir).replace('\\', '/')
              declaration.findAll(text).forEach { match ->
                types++
                val type = match.groupValues[1]
                val link = "$expectedUrl#type_$type"
                if (!text.contains(link)) {
                  val line = text.take(match.range.first).count { it == '\n' } + 1
                  val other = Regex("""#type_(\w+)""").find(text)?.groupValues?.get(1)
                  errors +=
                      if (other != null && other != type) {
                        "$name:$line declares `$type` but links to the definition of `$other`"
                      } else {
                        "$name:$line declares `$type` without a link to $link"
                      }
                }
              }
            }

        if (!libraryVersion.startsWith("$expected.")) {
          errors +=
              "the version of the library ($libraryVersion) does not share the minor version " +
                  "$expected of the specification"
        }

        if (errors.isNotEmpty()) {
          throw GradleException(
              "Specification consistency check failed:" +
                  eol +
                  errors.joinToString(eol) { "  - $it" } +
                  eol +
                  "  Run `gradlew updateSpecVersion` to rewrite the derived mentions.")
        }
        report.get().asFile.apply {
          parentFile.mkdirs()
          writeText(
              "Specification version $expected, library version $libraryVersion, " +
                  "$mentions derived mentions and $types normative links verified " +
                  "against $expectedUrl" +
                  eol)
        }
      }
    }

/**
 * Rewrites every derived mention of the specification version to the value of `spec.apiVersion`,
 * and brings the version of the library onto that minor. Counterpart of `checkSpecConsistency`, in
 * the same way that `spotlessApply` answers `spotlessCheck`: the check is the gate, so this task
 * does not have to be exhaustive -- whatever it cannot reach, the check names.
 *
 * It does not touch the prose quoted from the specification: see CONTRIBUTING.md.
 */
tasks.register("updateSpecVersion") {
  description = "Rewrites the derived mentions of the specification version to spec.apiVersion."
  group = "documentation"
  notCompatibleWithConfigurationCache("rewrites files of the project")
  val sources = specVersionedFiles
  val patterns = specVersionPatterns
  val expected = specApiVersion
  val libraryVersion = project.version.toString()
  val projectDir = layout.projectDirectory.asFile
  val propertiesFile = layout.projectDirectory.file("gradle.properties").asFile
  doLast {
    var changed = 0

    sources.files
        .filter { it.isFile }
        .sortedBy { it.path }
        .forEach { file ->
          val before = file.readText()
          var after = before
          patterns.forEach { pattern ->
            after = pattern.replace(after) { match -> swapVersion(match, expected) }
          }
          if (after != before) {
            file.writeText(after)
            changed++
            logger.lifecycle("  updated ${file.toRelativeString(projectDir).replace('\\', '/')}")
          }
        }

    if (!libraryVersion.startsWith("$expected.")) {
      val target = "$expected.0-SNAPSHOT"
      val text = propertiesFile.readText()
      val updated = text.replace(Regex("""(?m)^version = .*$"""), "version = $target")
      if (updated != text) {
        propertiesFile.writeText(updated)
        changed++
        logger.lifecycle("  updated gradle.properties: version = $target")
      }
    }

    if (changed == 0) {
      logger.lifecycle("Everything already states version $expected.")
    } else {
      logger.lifecycle(
          "$changed file(s) updated. The prose quoted from the specification is NOT rewritten: " +
              "re-read it in the new revision.")
    }
  }
}

tasks.named("check") { dependsOn(checkJvmBytecode, checkSpecConsistency) }

///////////////////////////////////////////////////////////////////////////////
//  CLASS DIAGRAM
///////////////////////////////////////////////////////////////////////////////

// A detached configuration rather than `configurations.resolvable(...)`: this classpath belongs to
// one task and has no place in the dependency graph of the project, and the detached form is stable
// API where the role-based factories are still @Incubating.
val plantumlRuntime: FileCollection =
    configurations.detachedConfiguration(dependencies.create(libs.plantuml.get()))

/**
 * Renders the class diagram of the implementation, whose PlantUML source is edited separately from
 * the documentation that embeds it. The SVG is committed, so that reading the documentation never
 * requires a PlantUML installation.
 */
val renderUml =
    tasks.register<JavaExec>("renderUml") {
      description = "Renders docs/uml/class-diagram.puml into docs/uml/class-diagram.svg."
      group = "documentation"
      classpath = plantumlRuntime
      mainClass.set("net.sourceforge.plantuml.Run")
      args("-tsvg", "-nometadata", "-failfast2", "docs/uml/class-diagram.puml")
      inputs.file("docs/uml/class-diagram.puml")
      // The diagram embeds the logo, so a change to the assets must trigger a new render.
      inputs.dir("docs/assets").withPropertyName("assets")
      outputs.file("docs/uml/class-diagram.svg")
    }

///////////////////////////////////////////////////////////////////////////////
//  DOCUMENTATION
///////////////////////////////////////////////////////////////////////////////

dokka {
  moduleName.set(title)
  dokkaSourceSets.configureEach { includes.from("docs/Module.md") }
  dokkaPublications.html { outputDirectory.set(layout.buildDirectory.dir("dokka/html")) }
  pluginsConfiguration.html {
    footerMessage.set(gradleProperty("docs.copyright"))
    homepageLink.set(gradleProperty("docs.homepageLink"))
    // Dokka replaces its own logo with the asset named `logo-icon.svg`. The file is versioned here
    // rather than fetched from keypop.org, so that generating the documentation needs no network.
    customAssets.from(layout.projectDirectory.file("docs/assets/logo-icon.svg"))
    // The logo is a banner, not a square icon: this sheet restates the box Dokka reserves for it.
    customStyleSheets.from(layout.projectDirectory.file("docs/assets/keypop-logo.css"))
  }
}

// The class diagram is reached from the home page of the documentation only, so it is copied next
// to it rather than linked from the published list of versions.
val copyUmlToDokka =
    tasks.register<Copy>("copyUmlToDokka") {
      description = "Copies the class diagram into the Dokka output directory."
      dependsOn("dokkaGeneratePublicationHtml")
      from(renderUml)
      into(layout.buildDirectory.dir("dokka/html"))
    }

tasks.named("dokkaGeneratePublicationHtml") { finalizedBy(copyUmlToDokka) }

val dokkaHtmlJar =
    tasks.register<Jar>("dokkaHtmlJar") {
      dependsOn(copyUmlToDokka)
      description = "Assembles the KDoc of the public API."
      archiveClassifier.set("javadoc")
      from(layout.buildDirectory.dir("dokka/html"))
    }

///////////////////////////////////////////////////////////////////////////////
//  FORMATTING
///////////////////////////////////////////////////////////////////////////////

spotless {
  kotlin {
    target("src/**/*.kt")
    licenseHeaderFile(layout.projectDirectory.file("LICENSE_HEADER"))
    ktfmt()
  }
  kotlinGradle {
    target("**/*.kts")
    ktfmt()
  }
}

///////////////////////////////////////////////////////////////////////////////
//  PACKAGING AND PUBLISHING
///////////////////////////////////////////////////////////////////////////////

/**
 * Stages the license files that every archive carries under `META-INF`. A task rather than a script
 * function, so that the archives stay compatible with the configuration cache.
 */
val stageLicenseFiles =
    tasks.register<Sync>("stageLicenseFiles") {
      description = "Stages LICENSE and NOTICE.md for inclusion in the archives."
      from(layout.projectDirectory.file("LICENSE"))
      from(layout.projectDirectory.file("NOTICE.md"))
      into(layout.buildDirectory.dir("licenses/META-INF"))
    }

tasks.withType<Jar>().configureEach {
  from(stageLicenseFiles) { into("META-INF") }
  manifest {
    attributes(
        mapOf(
            "Implementation-Title" to title,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to gradleProperty("organization.name"),
            "Implementation-URL" to repoUrl,
            "Specification-Title" to title,
            "Specification-Version" to specApiVersion,
            "Specification-Vendor" to gradleProperty("organization.name")))
  }
}

publishing {
  publications.withType<MavenPublication>().configureEach {
    artifact(dokkaHtmlJar)
    pom {
      name.set(title)
      description.set(gradleProperty("description"))
      url.set(repoUrl)
      licenses {
        license {
          name.set(gradleProperty("license.name"))
          url.set(gradleProperty("license.url"))
          distribution.set(gradleProperty("license.distribution"))
        }
      }
      developers {
        developer {
          name.set(gradleProperty("developer.name"))
          email.set(gradleProperty("developer.email"))
        }
      }
      organization {
        name.set(gradleProperty("organization.name"))
        url.set(gradleProperty("organization.url"))
      }
      scm {
        connection.set(scmConnection)
        developerConnection.set(scmDeveloperConnection)
        url.set(repoUrl)
      }
      ciManagement {
        system.set(gradleProperty("ci.system"))
        url.set(ciUrl)
      }
      properties.set(
          mapOf(
              "project.build.sourceEncoding" to "UTF-8",
              "maven.compiler.target" to javaTargetLevel,
              "specification.version" to specApiVersion,
              "specification.url" to specDocUrl))
    }
  }
  repositories {
    maven {
      val sonatypeUrl: Provider<String> = providers.gradleProperty("sonatypeURL")
      if (sonatypeUrl.isPresent) {
        url = uri(sonatypeUrl.get())
        credentials {
          username = gradleProperty("sonatypeUsername")
          password = gradleProperty("sonatypePassword")
        }
      }
    }
  }
}

signing {
  if (releaseTag.isPresent) {
    useGpgCmd()
    sign(publishing.publications)
  }
}
