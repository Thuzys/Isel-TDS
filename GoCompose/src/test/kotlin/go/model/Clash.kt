package go.model

import go.storage.Storage

typealias GameStorage = Storage<String, Game>

private const val FIRST_PLAYER = 1
private const val SECOND_PLAYER = 2
private const val MAX_PLAYERS = 2

open class Clash(val gs: GameStorage)
class ClashRun(
        gs: GameStorage,
        val id: String,
        val me: Player,
        val game: Game,
): Clash(gs)

fun ClashRun.play(toPosition: Position): Clash{
    check((game.board as BoardRun).turn == me){
        "Not your turn"
    }
    val gameAfter = game.play(toPosition)

    gs.update(id, gameAfter)
    return ClashRun(gs, id, me, gameAfter)
}

fun Clash.startClash(name: String): Clash {
    val game = Game(firstPlayer = Player.X, players = FIRST_PLAYER).newBoard()
    gs.create(name, game)
    return ClashRun(gs, name, Player.X, game)
}

fun Clash.joinClash(name: String): Clash{
    val game = gs.read(name) ?: error("Clash %id not found")
    check(game.players < MAX_PLAYERS) {"Clash is already full"}
    val gameWithSecondPlayer = game.copy(players = SECOND_PLAYER)
    gs.update(name, gameWithSecondPlayer)
    return ClashRun(gs, name, Player.O, gameWithSecondPlayer)
}

fun ClashRun.refreshClash(): Clash {
    val game = gs.read(id) ?: error("Clash $id not found")
    return ClashRun(gs, id, me, game)
}

fun  Clash.deleteIfIsOwner(){
    if (this is ClashRun && me==Player.X)
            gs.delete(id)
}

fun Clash.pass(): Clash{
    check(this is ClashRun) {"Clash not start"}
    val game = game.pass()
    gs.update(id, game)
    return ClashRun(gs, id, me, game)
}
