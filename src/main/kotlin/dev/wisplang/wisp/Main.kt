package dev.wisplang.wisp

import dev.wisplang.wisp.lexer.Lexer
import java.io.File


fun main(args: Array<String>) {
    val tokenList = Tokenizer.tokenize(File("./test/test.wsp").inputStream().readBytes().toString(Charsets.UTF_8))
    val matureTokens = Tokenizer.matureTokens(tokenList)
    val root = Lexer.lex(matureTokens)
    println(root)
}

