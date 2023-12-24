package go.model

enum class Size(val sub: Double, val value: Int){
    NINE(3.5, 9),
//    THIRTEEN(4.5, 13),
//    NINETEEN(5.5, 19)
}

val BOARD_SIZE = Size.NINE
val BOARD_CELLS = BOARD_SIZE.value * BOARD_SIZE.value

typealias BoardCells = Map<Position, Player>
sealed class Board(val boardCells: BoardCells)
class BoardRun(
    boardCells: BoardCells,
    val turn: Player,
    val pass: Boolean = false,
    val removed: Player? = null): Board(boardCells)
class BoardWin(boardCells: BoardCells, val winner: Player): Board(boardCells)
class BoardDraw(boardCells: BoardCells): Board(boardCells)
fun Board(start: Player = Player.X): Board =  BoardRun(emptyMap(), start)
fun Board.play(playPositionIdx: Position): Board = when(this){
    is BoardRun -> {
        boardCells.isValid(playPositionIdx, turn)
        val boardCellsAfterPlay = boardCells + (playPositionIdx to turn)
        val (returnBoard, removed) = boardCellsAfterPlay.calcPlay()
        BoardRun(returnBoard, turn.other, removed = removed)
    }
    is BoardWin, is BoardDraw -> error("Game Over.")
}
private fun BoardCells.isValid(pos: Position, turn: Player){
    require(get(pos) == null){"Position ${pos.idx} is used."}
    val suicide = pos.getAdj().filterNotNull().fold(true){acc, adjPos ->
        acc && get(adjPos)?.name != turn.name && get(adjPos) != null
    }
    if(suicide) throw IllegalArgumentException("Suicide Move.")
}
fun BoardRun.pass() = BoardRun(boardCells, turn.other, true)
private fun BoardCells.getGroup(pos: Position, type: Player? = null, group: Group? = null): Group{
    var finalGroup = group?.plus(pos) ?: listOf(pos)
    pos.getAdj().filterNotNull().forEach { dirPos ->
        if (get(dirPos) == type && dirPos !in finalGroup)
            finalGroup = getGroup(dirPos, type, finalGroup)
    }
    return finalGroup
}
private fun BoardCells.removeGroup(group: Group, type: Player): Boolean{
    group.forEach { pos ->
        val remove = pos.getAdj().filterNotNull().filter{ !group.contains(it) }
            .fold(true){ acc, sidePos -> acc && (get(sidePos) != null) && (get(sidePos) != type) }
        if (!remove)
            return false
    }
    return true
}
private fun BoardCells.calcPlay(): Pair<BoardCells, Player?>{
    val positionsToMaintain = mutableListOf<Position>()
    var removed: Player? = null
    for (item in this.entries){
        if (item.key !in positionsToMaintain) {
            val group = getGroup(item.key, item.value)
            if (!removeGroup(group, item.value))
                positionsToMaintain += group
            else
                removed = item.value
        }
    }
    return filter { it.key in positionsToMaintain } to removed
}
private fun BoardCells.tellIfSurrounded(group: Group): Pair<Player, Int>?{
    var owner: Player? = null
    group.forEach { pos ->
        pos.getAdj().filterNotNull()
            .forEach {
                val player = get(it)
                if (player != null)
                    if (owner == null)
                        owner = player
                    else if (owner != player)
                        return null
            }
    }
    return owner?.let { it to group.size }
}
fun BoardCells.tellVoidCellsPoints(): Map<Player, Int>{
    val points = mutableMapOf<Player, Int>()
    val groups = mutableListOf<Position>()
    Position.values.filter { !keys.contains(it) }.forEach {
        if (it !in groups) {
            val group = getGroup(it)
            groups += group
            tellIfSurrounded(group)?.let { playerPoint ->
                points[playerPoint.first] = points[playerPoint.first]?.plus(playerPoint.second) ?: playerPoint.second
            }
        }
    }
    return points
}