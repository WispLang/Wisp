package io.github.wisplang


fun main(args: Array<String>) {
    val stringInput = readLine()!!
    val token = Tokenizer()
    var tokenList = token.lex(stringInput)
    tokenList.forEach { token -> println("${token.type}, ${token.value}")  }
}

