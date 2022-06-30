package io.github.wisplang

object Tokenizer {
    enum class Type {
        LEFTPARENTHESIS,
        RIGHTPARENTHESIS,
        INTEGER,
        STRING
    }

    enum class Symbol(val character: Char) {
        DIVISION('/'),
        MULTIPLY('*'),
        ADD('+'),
        SUBTRACT('-')
    }

    data class Token(val type: Type, val value: String)

    fun tokenize(value: String): ArrayList<Token> {
        val tokenArray = arrayListOf<Token>()

        var i = 0
        while(i < value.length) {
            val character = value[i]
            when {
                character == '"' -> {
                    value.dropLast(value.length)
                    val string = buildQuotedString(value,++i)
                    i = value.indexOf('"', i)
                    tokenArray.add(Token(Type.STRING, string))
                }
                character.isDigit() -> {
                    val integerString = buildNumString(value, i)
                    i+=integerString.length-1
                    tokenArray.add(Token(Type.INTEGER, integerString))
                }
                character == ')' -> {
                    tokenArray.add(Token(Type.RIGHTPARENTHESIS, character.toString()))
                }
                character == '(' -> {
                    tokenArray.add(Token(Type.LEFTPARENTHESIS, character.toString()))
                }
            }
            i++
        }
        return tokenArray
    }

    fun buildNumString(value: String, index: Int): String {
        var integerString = ""
        var hadDecimal = false
        for(i in index..value.length-1) {
            val char = value[i]
            if(char.isDigit() ) {
                integerString += char
            } else if (!hadDecimal && char == '.') {
                hadDecimal = true
                integerString += char
            } else if (char == '.') {
                //TODO: Replace with dedicated exception
                throw Exception("Wisp: Cannot have multiple decimal points in a number")
            }
        }
        return integerString
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




}