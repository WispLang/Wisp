package io.github.wisplang

import java.io.File


fun main(args: Array<String>) {
    val tokenList = Tokenizer.tokenize(File("./test/test.wsp").inputStream().readBytes().toString(Charsets.UTF_8))
    val matureTokens = Tokenizer.matureTokens(tokenList)
    matureTokens.forEach { println("${it.type}, ${it.value}: ${it.idx}, ${it.len}")  }
}

