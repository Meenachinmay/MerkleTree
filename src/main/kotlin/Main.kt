package org.polarmeet

import java.io.File

// Main.kt
fun main() {
    val demo = GitMerkleDemo()

    // Initialize demo files
    println("Initializing demo files...")
    demo.initializeFiles()

    // Build initial project state
    println("\nBuilding initial project state...")
    demo.buildProject()

    // Check initial status
    println("\nChecking initial status:")
    demo.checkStatus()

    // Modify a file
    println("\nModifying file2.txt...")
    File("demo_files/file2.txt").writeText("Modified content in file 2")

    // Check status after modification
    println("\nChecking status after modification:")
    demo.checkStatus()
}