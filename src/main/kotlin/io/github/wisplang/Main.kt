package io.github.wisplang


fun main(args: Array<String>) {
    while (true) {
        val stringInput = readLine()!!
        val tokenList = Tokenizer.tokenize(stringInput)
        tokenList.forEach { println("${it.type}, ${it.value}: ${it.startIdx}, ${it.endIdx}")  }
    }
}

