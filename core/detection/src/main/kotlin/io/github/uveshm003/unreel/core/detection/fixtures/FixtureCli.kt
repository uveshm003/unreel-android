package io.github.uveshm003.unreel.core.detection.fixtures

import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.serialization.json.Json

private const val ARGUMENT_COUNT = 3

fun main(args: Array<String>) = convertFixture(args, File("core/detection/src/main/rules/labels"))

internal fun convertFixture(args: Array<String>, labelsDirectory: File) {
    require(args.size == ARGUMENT_COUNT) { "Expected <package> <input.xml|input.json> <output.json>" }
    val (packageName, inputName, outputName) = args
    require(Regex("[A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)+").matches(packageName)) { "Invalid package name" }
    val input = File(inputName)
    val output = File(outputName)
    require(input.canonicalFile != output.canonicalFile) { "Choose a separate output file" }
    require(input.extension in setOf("xml", "json")) { "Expected an XML or JSON input file" }
    val labels = File(labelsDirectory, "$packageName.json")
    val result = runCatching {
        val allowed = if (labels.exists()) Json.decodeFromString<Set<String>>(labels.readText()) else emptySet()
        FixtureSanitiser(allowed).sanitise(input.readText(), xml = input.extension == "xml")
    }.getOrElse { throw IllegalArgumentException("Cannot sanitise fixture. Check input structure and label file.") }
    writeFixture(output, result)
}

private fun writeFixture(output: File, json: String) {
    val target = output.toPath().toAbsolutePath()
    val temp = Files.createTempFile(target.parent, ".fixture-", ".tmp")
    try {
        temp.toFile().writeText(json)
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        Files.deleteIfExists(temp)
    }
}
