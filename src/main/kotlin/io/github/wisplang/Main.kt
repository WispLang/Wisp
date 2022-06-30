package io.github.wisplang


fun main(args: Array<String>) {
    val stringInput = readLine()!!
    val tokenList = Tokenizer.tokenize(stringInput)
    tokenList.forEach { println("${it.type}, ${it.value}")  }
}

