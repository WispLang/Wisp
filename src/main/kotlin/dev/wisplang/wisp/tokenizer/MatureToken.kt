package dev.wisplang.wisp.tokenizer

data class MatureToken( val type: MatureType, val value: String, val idx: Int, val len: Int, val line: Int, val col: Int )
