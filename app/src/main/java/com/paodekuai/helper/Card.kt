// 扑克牌数据类
package com.paodekuai.helper

data class Card(val suit: String, val rank: Int) {
    // suit: "spade"黑桃 "heart"红心 "club"梅花 "diamond"方块
    // rank: 3-15 (3,4,5,...,10,J=11,Q=12,K=13,A=14,2=15)

    override fun toString(): String {
        val rankStr = when (rank) {
            11 -> "J"; 12 -> "Q"; 13 -> "K"; 14 -> "A"; 15 -> "2"
            else -> rank.toString()
        }
        val suitStr = when (suit) {
            "spade" -> "♠"; "heart" -> "♥"; "club" -> "♣"; "diamond" -> "♦"
            else -> suit
        }
        return "$suitStr$rankStr"
    }

    companion object {
        fun fromString(s: String): Card? {
            if (s.length < 2) return null
            val suit = when (s[0]) {
                '♠' -> "spade"; '♥' -> "heart"; '♣' -> "club"; '♦' -> "diamond"
                else -> return null
            }
            val rank = when (s.substring(1)) {
                "J" -> 11; "Q" -> 12; "K" -> 13; "A" -> 14; "2" -> 15
                else -> s.substring(1).toIntOrNull() ?: return null
            }
            return Card(suit, rank)
        }
    }
}
