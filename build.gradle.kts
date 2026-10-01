import java.io.ByteArrayOutputStream

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}

fun getGitCommitCount(): Int {
    val stdout = ByteArrayOutputStream()
    return try {
        exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim().toInt()
    } catch (_: Exception) {
        1
    }
}

fun getGitTagName(): String {
    val stdout = ByteArrayOutputStream()
    return try {
        exec {
            commandLine("git", "describe", "--tags", "--long", "--always")
            standardOutput = stdout
        }
        val rawOutput = stdout.toString().trim()

        val cleaned = rawOutput.replace(Regex("^v"), "")
        val parts = cleaned.split("-")

        if (parts.size >= 2) {
            val tagPart = parts[0]
            val commitsSinceTag = parts[1].toInt()

            val versionDigits = tagPart.split(".").toMutableList()
            while (versionDigits.size < 3) { versionDigits.add("0") }

            val patchVersion = versionDigits[2].toInt() + commitsSinceTag
            versionDigits[2] = patchVersion.toString()

            versionDigits.joinToString(".")
        } else {
            "1.0.0"
        }
    } catch (_: Exception) {
        "1.0.0"
    }
}

extra["appVersionCode"] = getGitCommitCount()
extra["appVersionName"] = getGitTagName()
