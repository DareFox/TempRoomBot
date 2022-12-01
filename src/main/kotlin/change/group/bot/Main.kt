package change.group.bot

import change.group.bot.extensions.PingPongExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot

suspend fun main(args: Array<String>) {
    val bot = ExtensibleBot(Config.botToken) {
        // To add extensions, you need to pass reference to extension class via :: symbol
        extensions {
            add(::PingPongExtension)
        }
    }
    bot.start()
}