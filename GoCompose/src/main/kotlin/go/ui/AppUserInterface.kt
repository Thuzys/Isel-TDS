package go.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mongodb.MongoWriteException
import go.mongo.MongoDriver
import go.storage.GameSerializer
import go.storage.MongoStorage
import go.model.*
import kotlinx.coroutines.*

typealias Score = Map<Player?, Double>
typealias Mongo = MongoStorage<String, Game>

class AppUserInterface(driver: MongoDriver, val scope: CoroutineScope) {
    private val storage = Mongo("games", driver, GameSerializer)
    private var clash by mutableStateOf(Clash(storage))

    var viewScore by mutableStateOf(false)
        private set
    var inputName by mutableStateOf<InputName?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    val game: Game? get() = (clash as? ClashRun)?.game
    val board: Board? get() = (clash as? ClashRun)?.game?.board
    val score: Score? get() = game?.score
    val canShowScore: Boolean get() = (board is BoardWin) || (board is BoardDraw)
    val pass: Boolean get() = (clash is ClashRun) && turnAvailable
    var showLastPos by mutableStateOf(false)
        private set
    val me: Player? get() = (clash as? ClashRun)?.me
    val hasClash: Boolean get() = clash is ClashRun
    private var waitingJob by mutableStateOf<Job?>(null)
    val isWaiting: Boolean get() = waitingJob != null
    private val turnAvailable: Boolean
        get() = (board as? BoardRun)?.turn == me

    fun hideError(){
        errorMessage = null
    }

    fun passPlay(){
        try {
            check(clash is ClashRun) { "Clash not started" }
            clash = clash.pass()
            waitForOtherSide()
        }catch (e: Exception){
            errorMessage = e.message
        }
    }

    fun play(pos: Position?){
        try {
            checkNotNull(pos) { "Invalid Position" }
            clash = clash.play(pos)
            waitForOtherSide()
        }catch (e: Exception){
            errorMessage = e.message
        }
    }

    enum class InputName(val txt: String){
        NEW("Start"), JOIN("Join")
    }
    fun cancelInput(){
        inputName = null
    }
    fun  newGame(gameName: String){
        try {
            cancelWaiting()

            clash = clash.startClash(gameName)
            inputName = null
        }catch (e: Exception){
            errorMessage = e.message
        }
    }

    fun joinGame(gameName: String){
        try {
            cancelWaiting()

            clash = clash.joinClash(gameName)
            inputName = null

            waitForOtherSide()
        }catch (e: Exception){
            errorMessage = e.message
        }
    }

    suspend fun refreshGame(){
        try {
            clash = clash.refreshClash()
        }catch (e: Exception){
            errorMessage = e.message
        }
    }

    fun showNewGameDialog(){
        inputName = InputName.NEW
    }
    fun showJoinGameDialog(){
        inputName = InputName.JOIN
    }

    fun exit(){
        clash.deleteIfIsOwner()
        cancelWaiting()
    }

    fun showLastPos(change: Boolean){
        showLastPos = change
    }

    private fun  cancelWaiting(){
        waitingJob?.cancel()
        waitingJob = null
    }

    private fun waitForOtherSide(){
        if (turnAvailable) return
        waitingJob = scope.launch(Dispatchers.IO) {
            do {
                delay(3000)
                try {
                    refreshGame()
                }catch (e : NoChangesException){ /* Ignore */ }
                catch (e : Exception){
                    errorMessage = e.message
                    if (e is GameDeletedException) clash = Clash(storage)
                }
            } while (!turnAvailable)
            waitingJob = null
        }
    }
}


