fun main() {
    // Example string containing hidden null bytes
    val corruptedText = "Hello\u0000World!\u0000"

    println("--- Analyzing Text ---")
    
    // 1. Locate the exact positions of the null bytes
    corruptedText.forEachIndexed { index, char ->
        if (char == '\u0000') {
            println("Invisible Null Byte found at index position: $index")
        }
    }

    // 2. Make them visible by replacing them with a marker
    val visibleText = corruptedText.replace("\u0000", "[NULL]")
    println("\nRevealed Text:\n$visibleText")

    // 3. Automatically delete them completely
    val cleanedText = corruptedText.replace("\u0000", "")
    println("\nCleaned Text (Bytes Removed):\n$cleanedText")
    println("Original length: ${corruptedText.length} | Cleaned length: ${cleanedText.length}")
}
