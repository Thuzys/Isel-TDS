package go.view

import go.model.*
import go.storage.Storage

class Command(
        val argSyntax: String = "",
        val isToFinish: Boolean = false,
        val execute: Clash.(List<String>) -> Clash = {_ -> throw IllegalStateException("Game Over")}
)

fun play(): Command =
        Command(argSyntax = "position"){ args ->
            check(this is ClashRun){"Game not started"}

            val arg = requireNotNull(args.firstOrNull()){"Missing index"}

            this.play(arg.toPosition())
        }

fun getCommands(): Map<String, Command>{
    return mapOf(
            "PLAY" to play(),
            "EXIT" to Command(isToFinish = true){_ ->
                this.also {
                    it.deleteIfIsOwner()
                }
            },
            "SCORE" to Command { _ ->
                this.also {
                    check(it is ClashRun) { "Game not started" }
                    it.game.showScore()
                }
            },
            "START" to Command("name"){args ->
                val name = requireNotNull(args.firstOrNull()) {"Missing name"}
                this.startClash(name)
            },
            "JOIN" to Command("name"){args ->
                val name = requireNotNull(args.firstOrNull()) {"Missing name"}
                this.joinClash(name)
            },
            "REFRESH" to Command{_ ->
                check(this is ClashRun) {"Game not started"}
                this.refreshClash()
            },
            "PASS" to Command{_ ->
                this.pass()
            },
            "SCORE" to Command{_ ->
                check(this is ClashRun) {"Clash not Started"}
                also { showScore() }
            }
    )
}


