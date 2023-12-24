package go.storage

import go.model.Game
import go.model.getNonNullablePlayer
import go.model.getPlayer

object GameSerializer : Seriealizer<Game> {
    override fun serialize(data: Game)  = buildString {
        appendLine( data.captured.entries.joinToString(" "){ (player, pts) ->
            "$player=$pts"
        } )
        appendLine( data.firstPlayer )
        data.board?.let { appendLine(BoardSerializer.serialize(it)) }
        appendLine( data.score.entries.joinToString(" ") { (player, score) ->
            "$player=$score"
        })
        appendLine( data.players )
    }

    override fun deserialize(text: String): Game =
        text.split("\n").let { (players, firstPlayer, board, score, playersNumber) -> Game(
            firstPlayer = getNonNullablePlayer(firstPlayer),
            captured = players.split(" ").map { it.split("=") }
                .associate { (player, points) ->
                    getPlayer(player) to points.toInt()
                },
            board = if (board.isBlank()) null else BoardSerializer.deserialize(board),
            score = score.split(" ").map { it.split("=") }
                .associate { (player, score) -> getPlayer(player) to score.toDouble() },
            players = playersNumber.toInt()
        ) }
}