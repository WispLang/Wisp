package dev.wisplang.wispc.util

import dev.wisplang.wispc.lexer.Lexer
import dev.wisplang.wispc.tokenizer.MatureToken
import dev.wisplang.wispc.tokenizer.MatureType

object TokenMatch {
    fun Lexer.match(token: MatureToken = consume(), function: MatchBody.() -> Unit) {
        val body = MatchBody()
        function(body)
        for ((key, value) in body.cases)
            if (key.first == token.type && (key.second == null || key.second == token.value))
                return value(token)
        body.defaultBody(token)
    }

    class MatchBody {
        internal val cases: MutableMap<Pair<MatureType, String?>, (MatureToken) -> Unit> = HashMap()
        internal var defaultBody: MatureToken.() -> Unit = { }

        fun on(type: MatureType, value: String? = null, function: MatureToken.() -> Unit) {
            cases[Pair(type, value)] = function
        }

        fun on(type: MatureType, vararg values: String, function: MatureToken.() -> Unit) {
            for (value in values)
                cases[Pair(type, value)] = function
        }

        fun on(vararg types: MatureType, function: MatureToken.() -> Unit) {
            for (type in types)
                cases[Pair(type, null)] = function
        }

        fun default(function: MatureToken.() -> Unit) {
            defaultBody = function
        }
    }
}
