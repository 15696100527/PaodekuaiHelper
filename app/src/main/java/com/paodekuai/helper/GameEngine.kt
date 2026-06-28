// 跑的快 游戏引擎 - 算牌逻辑
// 功能：分析手牌、计算胜率、推荐出牌
package com.paodekuai.helper

import kotlin.math.*

data class CardCombo(val type: String, val cards: List<Card>, val mainRank: Int) {
    // type: "single", "pair", "triple", "straight", "flush", "fullhouse", "bomb"
}

object GameEngine {

    /**
     * 分析当前手牌，返回分析建议
     * hand: 我的手牌
     * played: 已打出的牌（用于记牌）
     */
    fun analyze(hand: List<Card>, played: List<Card>): String {
        if (hand.isEmpty()) return "无手牌"
        val sb = StringBuilder()
        sb.appendLine("🃏 手牌分析（${hand.size} 张）")

        // 1. 按花色分组
        val bySuit = hand.groupBy { it.suit }
        sb.appendLine("  花色分布: ${bySuit.map { "${it.key}=${it.value.size}" }.joinToString()}")

        // 2. 查找顺子机会
        val straights = findPotentialStraights(hand)
        if (straights.isNotEmpty()) {
            sb.appendLine("  ✅ 潜在顺子: ${straights.joinToString { it.joinToString(",") { c -> c.toString() } }}")
        }

        // 3. 查找对子/三条
        val groups = hand.groupBy { it.rank }.filter { it.value.size >= 2 }
        if (groups.isNotEmpty()) {
            sb.appendLine("  ✅ 对子/多张: ${groups.map { "${rankStr(it.key)}×${it.value.size}" }.joinToString()}")
        }

        // 4. 记牌：推算剩余大牌
        val remaining = calculateRemaining(hand, played)
        if (remaining.isNotEmpty()) {
            sb.appendLine("  📊 剩余大牌: ${remaining.take(10).joinToString { it.toString() }}")
        }

        // 5. 胜率估算（简化）
        val winRate = estimateWinRate(hand)
        sb.appendLine("  📈 预计胜率: ${"%.0f".format(winRate * 100)}%")

        // 6. 出牌建议
        val suggestion = suggestPlay(hand)
        sb.appendLine("  💡 建议: $suggestion")

        return sb.toString()
    }

    /**
     * 找出潜在顺子（差1-2张即可成顺）
     */
    private fun findPotentialStraights(hand: List<Card>): List<List<Card>> {
        val ranks = hand.map { it.rank }.distinct().sorted()
        val results = mutableListOf<List<Card>>()
        // 找长度>=4的连续序列
        var i = 0
        while (i < ranks.size) {
            var j = i
            while (j + 1 < ranks.size && ranks[j + 1] - ranks[j] == 1) {
                j++
            }
            if (j - i + 1 >= 4) {
                val seq = ranks.subList(i, j + 1)
                val cards = seq.map { r -> hand.find { it.rank == r }!! }
                results.add(cards)
            }
            i = j + 1
        }
        return results
    }

    /**
     * 计算剩余未出现的牌（记牌功能）
     */
    private fun calculateRemaining(hand: List<Card>, played: List<Card>): List<Card> {
        val allCards = mutableListOf<Card>()
        for (suit in listOf("spade", "heart", "club", "diamond")) {
            for (rank in 3..15) {
                allCards.add(Card(suit, rank))
            }
        }
        val seen = (hand + played).map { "${it.suit}-${it.rank}" }.toSet()
        return allCards.filter { "${it.suit}-${it.rank}" !in seen }
            .sortedWith(compareByDescending<Card> { it.rank }.thenBy({ it.suit }))
    }

    /**
     * 简化胜率估算
     * 基于：手牌平均点数、剩余大牌数量、手牌张数
     */
    private fun estimateWinRate(hand: List<Card>): Double {
        if (hand.isEmpty()) return 0.0
        val avgRank = hand.map { it.rank }.average()
        // 平均点数越低越好（3-10比较好，JQKA偏大）
        val rankScore = (15.0 - avgRank) / 15.0  // 0-1，越高越好
        val sizeScore = when (hand.size) {
            in 0..5 -> 0.8  // 手牌少，快赢了
            in 6..10 -> 0.5
            else -> 0.2
        }
        // 有没有2（最大牌）
        val hasTwo = hand.any { it.rank == 15 }
        val twoBonus = if (hasTwo) 0.15 else 0.0
        return min(1.0, rankScore * 0.4 + sizeScore * 0.4 + twoBonus)
    }

    /**
     * 出牌建议
     */
    private fun suggestPlay(hand: List<Card>): String {
        if (hand.size <= 2) return "🎉 快出完！优先出最小牌"
        // 找最小单张
        val minCard = hand.minByOrNull { it.rank }
        // 找是否有人出过牌（简化：建议出最小能出的）
        val pairs = hand.groupBy { it.rank }.filter { it.value.size >= 2 }
        return when {
            pairs.isNotEmpty() -> "建议先出对子: ${rankStr(pairs.keys.min())}×${pairs.values.first().size}"
            minCard != null -> "建议出单张: ${minCard.toString()}"
            else -> "观察局势，保留大牌"
        }
    }

    private fun rankStr(rank: Int): String = when (rank) {
        11 -> "J"; 12 -> "Q"; 13 -> "K"; 14 -> "A"; 15 -> "2"
        else -> rank.toString()
    }
}
