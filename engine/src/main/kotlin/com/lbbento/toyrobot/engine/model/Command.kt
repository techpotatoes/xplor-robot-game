package com.lbbento.toyrobot.engine.model

sealed interface Command {
    data class Place(
        val position: Position,
        val direction: Direction,
    ) : Command

    data object Move : Command
    data object Left : Command
    data object Right : Command
}
