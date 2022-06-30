package io.github.wisplang

object Tokenizer {
    enum class Type {
        SYMBOL,
        NUMBER,
        STRING,
        NAME,
        WHITESPACE
    }
    data class Token(val type: Type, val value: String, val startIdx: Int, val endIdx: Int)

    val letterRegex = Regex("[a-zA-Z_$]")
    val symbolRegex = Regex("[{}()<>,.'\"\\[\\]|\\\\+\\-=*/&:]")

    fun tokenize(value: String): ArrayList<Token> {
        val tokenArray = arrayListOf<Token>()

        var i = 0
        while(i < value.length) {
            val character = value[i]
            val startIdx = i
            when {
                character == '"' -> {
                    value.dropLast(value.length)
                    val string = buildQuotedString(value,++i)
                    i += string.replace("\"","\\\"").replace("\n","\\n").length
                    tokenArray.add(Token(Type.STRING, string, startIdx, i))
                }
                character.isDigit() -> {
                    val integerString = buildNumString(value, i)
                    i+=integerString.length-1
                    tokenArray.add(Token(Type.NUMBER, integerString, startIdx, i))
                }
                symbolRegex.matches(character.toString()) -> {
                    tokenArray.add(Token(Type.SYMBOL, character.toString(), startIdx, i))
                }
                character.isWhitespace() -> {
                    tokenArray.add(Token(Type.WHITESPACE,character.toString(), startIdx, i))
                }
                letterRegex.matches(character.toString()) -> {
                        val nameString = buildNameString(value, i)
                        i+=nameString.length-1
                        tokenArray.add(Token(Type.NAME, nameString, startIdx, i))
                }


            }
            i++
        }
        return tokenArray
    }

    fun buildNumString(value: String, index: Int): String {
        var numberString = ""
        var hadDecimal = false
        for(i in index..value.length-1) {
            val char = value[i]
            if(char.isDigit() ) {
                numberString += char
            } else if (!hadDecimal && char == '.') {
                hadDecimal = true
                numberString += char
            } else if (char == '.') {
                //TODO: Replace with dedicated exception
                throw Exception("Wisp: Cannot have multiple decimal points in a number")
            } else if (char.isWhitespace()) {
                break
            }
        }
        return numberString
    }

    fun buildQuotedString(value: String, index: Int): String {
        var string = ""
        var i = index
        while (i < value.length) {
            val char = value[i]

            when (char) {
                '"' -> break
                '\\' -> {
                    val nextChar = value[++i]
                    string += when (nextChar) {
                        '"' -> '"'
                        'n' -> "\n"
                        else -> {"\\$nextChar"}
                    }
                }
                else -> string += char
            }
            i++
        }
        return string
    }

    fun buildNameString(value: String, index: Int): String {
        var string = ""
        var i = index
        while (i < value.length) {
            val char = value[i]

            if (letterRegex.matches(char.toString())) {
                string += char
            } else if (char.isWhitespace()) break
            i++
        }
        return string
    }
}