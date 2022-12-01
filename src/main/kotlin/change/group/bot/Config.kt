package change.group.bot

import java.time.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Config {
    val botToken: String
        get() = readEnv("DISCORD_BOT_TOKEN")

    val testGuildID: String
        get() = readEnv("DISCORD_TEST_GUILD_ID")

    val mongoUrl: String
        get() = readEnv("MONGODB_URL")

    val startTime = Instant.now().nano.toDuration(DurationUnit.NANOSECONDS)
}

private fun readEnv(name: String): String {
    val env = System.getenv(name)

    require(env != null) {
        "There is no environment variable called $name"
    }

    return env
}