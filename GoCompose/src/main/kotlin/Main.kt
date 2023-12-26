import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import go.model.*
import go.mongo.MongoDriver
import go.ui.AppUserInterface
import go.ui.AppUserInterface.InputName
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign

const val POSSIBLE_COL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
val CELL_SIDE = 40.dp
val CORRECTION = 15.dp
val BOARD_SIDE = CELL_SIDE * (BOARD_SIZE.value+2)
@Composable
@Preview
fun FrameWindowScope.App(driver: MongoDriver, exitFunction: () -> Unit){
    val scope = rememberCoroutineScope()
    val vm = remember { AppUserInterface(driver, scope) }

    MenuBar {
        Menu("Game"){
            Item("NewGame", onClick = vm::showNewGameDialog)
            Item("JoinGame", onClick = vm::showJoinGameDialog)
            Item("Exit", onClick = { vm.exit(); exitFunction() })
        }
        Menu("Play"){
            Item("Pass", enabled = vm.pass, onClick = vm::passPlay)
        }
        Menu("Options"){
            CheckboxItem("Show last position", checked = vm.showLastPos, onCheckedChange = { vm.showLastPos(it)})
        }
    }

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally){
            BoardView(
                boardCells = vm.board?.boardCells,
                onClick = vm::play,
                show = vm.showLastPos,
                position = vm.game?.lastPlay
            )
            StatusBar(vm.board, vm.me)
        }
        vm.inputName?.let {
            StartOrJoinDialog(
                type = it,
                onCancel = vm::cancelInput,
                onAction = if(it==InputName.NEW) vm::newGame else vm::joinGame
            )
        }
        vm.errorMessage?.let { ErrorDialog(it, onClose = vm::hideError) }
        if(vm.isWaiting) WaitingIndicator()
    }

}

@Composable
fun WaitingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        strokeWidth = 15.dp
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DialogBase(
    title: String,
    onClose: ()->Unit,
    content: @Composable ()->Unit
) = AlertDialog(
    onDismissRequest = onClose,
    title = { Text(title, style = MaterialTheme.typography.h4) },
    text = content,
    confirmButton = { TextButton(onClick = onClose) { Text("Close") } }
)

@Composable
fun ErrorDialog(message: String, onClose: ()->Unit) =
    DialogBase("Error", onClose) {
        Text(message, style = MaterialTheme.typography.h6)
    }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StartOrJoinDialog(
    type: InputName,
    onCancel: ()->Unit,
    onAction: (String)->Unit) {

    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "Name to ${type.txt}",
            style = MaterialTheme.typography.h5
        )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name of game") }
            )
        },
        confirmButton = {
            TextButton(enabled = true,
                onClick = { onAction(name)}
            ) { Text(type.txt) }
        },
        dismissButton = {
            TextButton(onClick = onCancel){ Text("Cancel") }
        }
    )
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalStdlibApi::class)
@Composable
fun ScoreDialog(score: Map<Player?, Int>, closeDialog: () -> Unit) =
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = { TextButton(onClick=closeDialog){ Text("Close") } },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column( horizontalAlignment = Alignment.CenterHorizontally){
                    Player.entries.forEach { player ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            //Cell( player, size = 20.dp)
                            Text(
                                text = " - ${score[player]}",
                                style = MaterialTheme.typography.h4
                            )
                        }
                    }
                    Text("Draws - ${score[null]}", style = MaterialTheme.typography.h4)
                }
            }
        }
    )
@Composable
fun StatusBar(board: Board?, me: Player?){
    Row(
        modifier = Modifier
            .height(CELL_SIDE+10.dp)
            .width(CELL_SIDE*(BOARD_SIZE.value+0.5f))
            .background(color = Color.LightGray)
    ) {
        val size = CELL_SIDE+10.dp
        me?.let {
            Text("You", style = MaterialTheme.typography.h4)
            ShowPlayer(player = it)
            Spacer(Modifier.width(30.dp))
        }
        val (text, player) = when(board){
            is BoardRun -> "Turn" to board.turn
            is BoardDraw -> "Draw" to null
            is BoardWin -> "Winner" to board.winner
            null -> "Game not started" to null
        }
        Text(
            text = text,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
                .fillMaxHeight()
        )
        if (player != null) ShowPlayer(player)
    }
}
@Composable
fun ShowPlayer(player: Player){
    val modifier = Modifier.fillMaxHeight()
    val filename = when (player){
        Player.X -> "blackStone.png"
        Player.O -> "whiteStone.png"
    }
    Image(
        painter = painterResource(filename),
        contentDescription = "Player $player",
        modifier = modifier
    )
}
@Composable
fun BoardView(boardCells: BoardCells?, show: Boolean, position: Position? ,onClick: (Position?) -> Unit) =
    Column(
        modifier = Modifier.width(CELL_SIDE*(BOARD_SIZE.value+0.5f))
            .height(CELL_SIDE*(BOARD_SIZE.value+1f))
            .paint(painterResource("board.png"),
            contentScale = ContentScale.FillBounds)){
        repeat(BOARD_SIZE.value+1) { row ->
            Row{
                repeat(BOARD_SIZE.value+1) { col ->
                    if (row == 0)
                        ShowColumnsIdx(col, CELL_SIDE)
                    else if(col == 0){
                        ShowRowIdx(row, CELL_SIDE)
                    }
                    else {
                        val pos = Position(row, col)
                        Cell(
                            boardCells?.get(pos),
                            onClick = { onClick(pos) },
                            row = row,
                            col = col,
                            show = show && pos == position
                        )
                    }
                }
            }
        }
    }
@Composable
fun ShowRowIdx(row: Int, size: Dp){
    val modifier = Modifier.size(size-CORRECTION, size)
//        .paint(painterResource("board.png"), contentScale = ContentScale.FillBounds)
    Box(modifier = modifier){
        Text(
            text = "${BOARD_SIZE.value-row+1}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6,
            modifier = modifier
        )
    }
}
@Composable
fun ShowColumnsIdx(col: Int,size: Dp){
    val modifier = Modifier.size(size-CORRECTION/9, size-CORRECTION)
//        .paint(painterResource("board.png"), contentScale = ContentScale.FillBounds)
    Box(modifier = modifier){
        when(col){
            0 -> Text(
                text = "    ",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6,
                modifier = modifier
            )
            else -> Text(
                text = "${POSSIBLE_COL[col-1]}  ",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6,
                modifier = modifier
            )
        }
    }
}

@Composable
fun Cell(
    player: Player?,
    size: Dp = 40.dp,
    onClick: () -> Unit= {},
    row: Int,
    col: Int,
    show: Boolean,
    ){
    val modifier = if (player == null)
        Modifier.size(size).clickable(onClick = onClick)
    else
        Modifier.size(size)
    Box(modifier = modifier.drawBehind {
        DrawCross(
            col = col,
            row = row,
            modifier = modifier
        )
    }){
        if (show)
            Box(
                modifier.background(color = Color.Transparent)){
                DrawRedBox(modifier)
            }
        if (player != null) {
            val filename = when (player) {
                Player.X -> "blackStone.png"
                Player.O -> "whiteStone.png"
            }
            Image(
                painter = painterResource(filename),
                contentDescription = "Player $player",
                modifier = modifier
            )
        }
    }
//    if (player == null){
//        Box(modifier.clickable(onClick = onClick)){
//            DrawCross(
//                col = col,
//                row = row,
//                modifier = modifier
//            )
//        }
//    }else{
//        val filename = when (player){
//            Player.X -> "blackStone.png"
//            Player.O -> "whiteStone.png"
//        }
//        Image(
//            painter = painterResource(filename),
//            contentDescription = "Player $player",
//            modifier = modifier
//        )
//        if (show)
//            Box(
//                modifier.background(color = Color.Transparent)){
//                DrawRedBox(modifier)
//            }
//    }
}

@Composable
fun DrawRedBox(modifier: Modifier){
    val strokeWidth = 2f
    Canvas(modifier){
        drawLine(
            color = Color.Red,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color.Red,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color.Red,
            start = Offset(size.width, size.height),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color.Red,
            start = Offset(size.width, size.height),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
    }
}


fun DrawScope.DrawCross(col: Int,row: Int, modifier: Modifier){
    val strokeWidth = 2f
    when{
        col == 1 && row == 1  -> {
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidth
            )
        }
        col == 1  && row == 9-> {
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(center.x, 0f),
                strokeWidth = strokeWidth
            )
        }
        col == 1 ->{
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidth
            )
        }
        col == 9 && row == 1 -> {
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(0f, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidth
            )
        }
        col == 9 && row == 9 -> {
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(0f, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(center.x, 0f),
                strokeWidth = strokeWidth
            )
        }
        col == 9 -> {
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(0f, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidth
            )
        }
        row == 1 ->{
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidth
            )
        }
        row == 9  -> {
            // Horizontal Line
            drawLine(
                color = Color.Black,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth
            )
            // Vertical Line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, center.y),
                end = Offset(center.x, 0f),
                strokeWidth = strokeWidth
            )
        }
        else -> {
            // Draw horizontal line
            drawLine(
                color = Color.Black,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth
            )

            // Draw vertical line
            drawLine(
                color = Color.Black,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.width),
                strokeWidth = strokeWidth
            )
        }
    }
}



fun main() =
    MongoDriver("GoCluster").use { driver ->
        application {
            Window(
                onCloseRequest = {},
                title = "Go game",
                state = WindowState(size = DpSize.Unspecified),
                resizable = false
            ){
                App(driver, ::exitApplication)
            }
        }
    }

//@Composable
//@Preview
//fun App() {
//    var text by remember { mutableStateOf("Hello, World!") }
//
//    MaterialTheme {
//        Button(onClick = {
//            text = "Hello, Desktop!"
//        }) {
//            Text(text)
//        }
//    }
//}
//
//fun main() = application {
//    Window(onCloseRequest = ::exitApplication) {
//        App()
//    }
//}
