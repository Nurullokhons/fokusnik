package com.fokusnik.model

enum class Suit(val symbol: String, val red: Boolean) {
    HEARTS("♥", true), DIAMONDS("♦", true), CLUBS("♣", false), SPADES("♠", false)
}

data class PlayingCard(val rank: String, val suit: Suit) {
    val label: String get() = rank + suit.symbol
}
