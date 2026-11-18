package com.fokusnik.engine

import com.fokusnik.model.PlayingCard
import com.fokusnik.model.Suit

/** Pure 21-card trick logic. The selected pile is always collected into the middle. */
object MagicEngine {
    fun newDeck(): List<PlayingCard> {
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val all = Suit.entries.flatMap { suit -> ranks.map { rank -> PlayingCard(rank, suit) } }
        return listOf(0, 14, 28, 3, 17, 31, 6, 20, 34, 9, 23, 37, 12, 26, 40, 1, 15, 29, 4, 18, 32).map(all::get)
    }

    fun dealIntoPiles(deck: List<PlayingCard>): List<List<PlayingCard>> {
        require(deck.size == 21) { "Для фокуса нужны ровно 21 карта" }
        return List(3) { column -> deck.filterIndexed { index, _ -> index % 3 == column } }
    }

    fun collect(piles: List<List<PlayingCard>>, selectedPile: Int): List<PlayingCard> {
        require(piles.size == 3 && piles.all { it.size == 7 })
        require(selectedPile in 0..2)
        val order = (0..2).filter { it != selectedPile }
        return piles[order[0]] + piles[selectedPile] + piles[order[1]]
    }

    fun reveal(deck: List<PlayingCard>): PlayingCard {
        require(deck.size == 21)
        return deck[10]
    }
}
