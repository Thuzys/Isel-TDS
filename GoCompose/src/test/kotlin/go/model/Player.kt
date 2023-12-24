package go.model

enum class Player(val simbol: Char, val color: String){
    O('O', "White"),
    X('#', "Black");
    val other get() = if(this == X) O else X
}
fun getPlayer(str: String) = Player.entries.firstOrNull { str == it.name }
fun getNonNullablePlayer(str: String) = checkNotNull(getPlayer(str)) {"Invalid player."}