package io.github.wisplang

class Tokenizer {

    enum class Type {
        LEFTPARENTHESIS,
        RIGHTPARENTHESIS,
        INTEGER,
        STRING
    }

    enum class Symbol(character: Char) {
        DIVISION('/'),
        MULTIPLY('*'),
        ADD('+'),
        SUBTRACT('-')
    }

    class Token(val type: Type, val value: String)

    fun lex(value: String): ArrayList<Token> {
        val tokenArray = arrayListOf<Token>()

        var i = 0
        while(i < value.length) {
            val character = value.get(i)
            when {
                character == '"' -> {
                    value.dropLast(value.length)
                    val string = addCharacterToString(value,i)
                    tokenArray.add(Token(Type.STRING, string))
                }
                character == ')' -> {
                    tokenArray.add(Token(Type.RIGHTPARENTHESIS, character.toString()))
                }
                character == '(' -> {
                    tokenArray.add(Token(Type.LEFTPARENTHESIS, character.toString()))
                }

                character.isDigit() -> {
                    val integerString = addIntToString(value, i)
                    i+=integerString.length-1
                    tokenArray.add(Token(Type.INTEGER, integerString))
                }

            }
            i++

        }
        return tokenArray
    }

    fun addIntToString(value: String, index: Int): String {
        var integerString = ""
        for(i in index..value.length-1) {
            val char = value.get(i)
            if(char.isDigit()) {
                integerString += char
            }
        }
        return integerString
    }

    fun addCharacterToString(value: String, index: Int): String {
        var string = ""
        var i = index
        while(i < value.length-1) {
            var char = value.get(i)
            if(char == '"') {
                i++
            }
            else {
                string += char
                i++
            }
        }
        return string
    }




}