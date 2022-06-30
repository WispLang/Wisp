package io.github.wisplang

object Tokenizer {
    enum class Type {
        SYMBOL,
        NUMBER,
        STRING,
        NAME,
        WHITESPACE,
        BOOLEAN
    }
    data class Token(val type: Type, val value: String)

    val letterRegex = Regex("[a-zA-Z_$]")
    val symbolRegex = Regex("[{}()<>,.'\"\\[\\]|\\\\+\\-=*/&]")

    fun tokenize(value: String): ArrayList<Token> {
        val tokenArray = arrayListOf<Token>()

        var i = 0
        while(i < value.length) {
            val character = value[i]
            when {
                character == '"' -> {
                    value.dropLast(value.length)
                    val string = buildQuotedString(value,++i)
                    i = string.replace("\"","\\\"").replace("\n","\\n").length
                    tokenArray.add(Token(Type.STRING, string))
                }
                character.isDigit() -> {
                    val integerString = buildNumString(value, i)
                    i+=integerString.length-1
                    tokenArray.add(Token(Type.NUMBER, integerString))
                }
                letterRegex.matches(character.toString()) && !(value.equals("true") || value.equals("false")) -> {
                    val nameString = buildNameString(value, i)
                    i+=nameString.length-1
                    tokenArray.add(Token(Type.NAME, nameString))
                }
                symbolRegex.matches(character.toString()) -> {
                    tokenArray.add(Token(Type.SYMBOL, character.toString()))
                }
                character.isWhitespace() -> {
                    tokenArray.add(Token(Type.WHITESPACE,character.toString()))
                }
                value.equals("true") || value.equals("false") -> {
                    tokenArray.add(Token(Type.BOOLEAN, value))
                    i+=value.length-1
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

            if (letterRegex.matches(char.toString()) || char.isDigit()) {
                string += char
            }
            i++
        }
        return string
    }
}