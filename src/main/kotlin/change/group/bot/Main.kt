package change.group.bot

import change.group.bot.extensions.PingPongExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent

@OptIn(PrivilegedIntent::class)
suspend fun main(args: Array<String>) {
    val bot = ExtensibleBot(Config.botToken) {
        // To add extensions, you need to pass reference to extension class via :: symbol
        extensions {
            add(::PingPongExtension)
        }
        intents {
            +Intents.all
        }
    }
    bot.start()
}