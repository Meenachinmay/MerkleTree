package org.polarmeet

import java.io.File
import java.security.MessageDigest

data class MerkleNode(
    val name: String,
    val hash: String,
    val content: String,  // Added content to track
    val children: List<MerkleNode> = emptyList()
)

class GitMerkleDemo {
    private val workingDirectory = "demo_files"
    private var previousTreeRoot: MerkleNode? = null

    fun initializeFiles() {
        File(workingDirectory).mkdir()

        File("$workingDirectory/file1.txt").writeText("Initial content of file 1")
        File("$workingDirectory/file2.txt").writeText("Initial content of file 2")
        File("$workingDirectory/file3.txt").writeText("Initial content of file 3")
    }

    private fun calculateHash(content: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(content.toByteArray())
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }

    fun buildProject() {
        val files = File(workingDirectory).listFiles()?.filter { it.isFile } ?: return

        val leafNodes = files.map { file ->
            val content = file.readText()
            val hash = calculateHash(content)
            MerkleNode(file.name, hash, content)
        }.sortedBy { it.name }  // Sort to maintain consistent order

        val rootHash = calculateHash(leafNodes.joinToString { it.hash })
        val root = MerkleNode("root", rootHash, "", leafNodes)

        previousTreeRoot = root
        println("Project state saved!")
    }

    fun checkStatus() {
        if (previousTreeRoot == null) {
            println("No previous state found. Run buildProject first.")
            return
        }

        val currentFiles = File(workingDirectory).listFiles()?.filter { it.isFile }?.sortedBy { it.name } ?: return
        val changes = mutableListOf<String>()

        currentFiles.forEach { file ->
            val currentContent = file.readText()
            val currentHash = calculateHash(currentContent)

            val previousNode = previousTreeRoot?.children?.find { it.name == file.name }

            if (previousNode == null) {
                changes.add("New file: ${file.name}")
            } else if (previousNode.hash != currentHash) {
                changes.add("Modified: ${file.name}")
                println("\nFile: ${file.name}")
                println("Previous content: ${previousNode.content}")
                println("Current content: $currentContent")
            }
        }

        if (changes.isEmpty()) {
            println("No changes detected")
        } else {
            println("\nChanges detected:")
            changes.forEach { println(it) }
        }

        println("\nCurrent Merkle Tree Structure:")
        printMerkleTree(previousTreeRoot!!, "")
    }

    private fun printMerkleTree(node: MerkleNode, prefix: String) {
        println("$prefix├── ${node.name} (${node.hash.take(8)}...)")
        node.children.forEach { child ->
            printMerkleTree(child, "$prefix│   ")
        }
    }

    fun modifyFile(fileNumber: Int, newContent: String) {
        val fileName = "file$fileNumber.txt"
        File("$workingDirectory/$fileName").writeText(newContent)
        println("File $fileName has been modified with new content: $newContent")
    }

    fun showFileContents() {
        println("\nCurrent file contents:")
        for (i in 1..3) {
            val content = File("$workingDirectory/file$i.txt").readText()
            println("File $i: $content")
        }
    }
}

fun main() {
    val demo = GitMerkleDemo()

    println("Initializing demo files...")
    demo.initializeFiles()

    println("\nBuilding initial project state...")
    demo.buildProject()

    while (true) {
        println("\nOptions:")
        println("1. Check status")
        println("2. Modify a file")
        println("3. Build project")
        println("4. Show file contents")
        println("5. Exit")
        println("Enter your choice (1-5):")

        when (readLine()) {
            "1" -> demo.checkStatus()
            "2" -> {
                println("Which file do you want to modify? (1-3):")
                val fileNum = readLine()?.toIntOrNull()
                if (fileNum in 1..3) {
                    println("Enter new content for file $fileNum:")
                    val newContent = readLine() ?: ""
                    if (fileNum != null) {
                        demo.modifyFile(fileNum, newContent)
                    }
                } else {
                    println("Invalid file number!")
                }
            }
            "3" -> demo.buildProject()
            "4" -> demo.showFileContents()
            "5" -> {
                println("Exiting...")
                return
            }
            else -> println("Invalid option!")
        }
    }
}