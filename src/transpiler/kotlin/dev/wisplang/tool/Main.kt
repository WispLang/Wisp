package dev.wisplang.tool

import dev.wisplang.wisp.lexer.Lexer
import dev.wisplang.wisp.tokenizer.Tokenizer
import java.io.File

fun main(args: Array<String>) {
    val tokenList = Tokenizer.tokenize(File(args[1]).inputStream().readBytes().toString(Charsets.UTF_8))
    val matureTokens = Tokenizer.matureTokens(tokenList)
    val root = Lexer().lex(matureTokens, args[1])

    when (args[0]) {
        "javailer" -> {
            val path = File("./src/java/")
            path.mkdirs()

            Transpiler(root, path).java()
        }
    }
}
