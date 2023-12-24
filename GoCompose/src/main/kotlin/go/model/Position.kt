package go.model

const val POSSIBLE_COL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
typealias Group = List<Position>
@JvmInline
value class Position private constructor(val idx: String){

    val line get() = idx.dropLast(1).toInt()
    val col get() = idx.last() - ('A'- 1)

    companion object {
        val values = List(BOARD_CELLS){
            val pos = "${BOARD_SIZE.value - (it/(BOARD_SIZE.value))}${POSSIBLE_COL[it%(BOARD_SIZE.value)]}"
            Position(pos)
        }
        @Suppress("NAME_SHADOWING")
        operator fun invoke(idx: String): Position{
            val idx = if(idx.last().uppercaseChar() in POSSIBLE_COL) idx else idx.switch()

            require(POSSIBLE_COL.take(BOARD_SIZE.value).contains(idx.last().uppercase())){"Invalid column."}
            require(idx.dropLast(1).toInt() in 1..BOARD_SIZE.value){"Invalid line."}
            val indice = (idx.dropLast(1).toInt() - BOARD_SIZE.value)*-BOARD_SIZE.value + (idx.uppercase().last() - 'A')
            return values[indice]
        }
        fun getIdx(position: Position) = values.indexOf(position)
    }
}

private fun String.switch() = "${drop(1)}${first()}"
fun String.toPosition() = Position(this)
fun Position.getAdj(): List<Position?>{
    val final = mutableListOf<Position?>()
    if (col - 1 == 0)  final.add(null) else final.add(Position.values[Position.getIdx(this) - 1])
    if (col + 1 > BOARD_SIZE.value) final.add(null) else final.add(Position.values[Position.getIdx(this) + 1])
    if (line + 1 > BOARD_SIZE.value) final.add(null) else final.add(Position.values[Position.getIdx(this) - BOARD_SIZE.value])
    if (line - 1 == 0) final.add(null) else final.add(Position.values[Position.getIdx(this) + BOARD_SIZE.value])
    return final
}