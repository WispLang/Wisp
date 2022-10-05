package dev.wisplang.wisp

import dev.wisplang.wisp.lexer.Lexer
import dev.wisplang.wisp.tokenizer.Tokenizer
import java.io.File


fun main(args: Array<String>) {
    val file = "./test/test.wsp"
    val tokenList = Tokenizer.tokenize(File(file).inputStream().readBytes().toString(Charsets.UTF_8))
    val matureTokens = Tokenizer.matureTokens(tokenList)
    val root = Lexer().lex(matureTokens, file)
    println(root)
}

