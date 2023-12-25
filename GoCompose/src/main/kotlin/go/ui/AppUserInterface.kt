package go.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    val board: Board? get() = (clash as? ClashRun)?.game?.board
    val score: Score get() = (clash as ClashRun).game.score
    val me: Player? get() = (clash as? ClashRun)?.me
    val hasClash: Boolean get() = clash is ClashRun
    private var waitingJob by mutableStateOf<Job?>(null)
    val isWaiting: Boolean get() = waitingJob != null
    private val turnAvailable: Boolean
        get() = (board as? BoardRun)?.turn == me

    fun hideError(){
        errorMessage = null
    }

    fun play(pos: Position){
        try {
            clash = clash.play(pos)
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
        cancelWaiting()

        clash = clash.startClash(gameName)
        inputName = null
    }

    fun joinGame(gameName: String){
        cancelWaiting()

        clash = clash.joinClash(gameName)
        inputName = null

        waitForOtherSide()
    }

    suspend fun refreshGame(){
        try {
            clash.refreshClash()
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
                    clash = clash.refreshClash()
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

