package change.group.bot.database.global

import change.group.bot.Config
import change.group.bot.models.PongCounterEntry
import com.mongodb.client.model.UpdateOptions
import dev.kord.common.entity.Snowflake
import org.litote.kmongo.*

object CounterCollection {
    /**
     * Get [PongCounterEntry] from collection
     */
    fun getCounter(snowflake: Snowflake): PongCounterEntry? {
        val db = KMongo.createClient(Config.mongoUrl)

        // Auto close DB resource with .use function
        val result = db.use {
            val collection = db
                .getDatabase("GlobalDiscord")
                .getCollection<PongCounterEntry>("Pings")

            val searchQuery = and(
                PongCounterEntry::authorSnowflake eq snowflake.toString()
            )

            collection.findOne(searchQuery)
        }

        return result
    }

    /**
     * Set or update [PongCounterEntry] in collection
     */
    fun setCounter(pongEntry: PongCounterEntry) {
        val db = KMongo.createClient(Config.mongoUrl)

        // Auto close DB resource with .use function
        db.use {
            val collection = db
                .getDatabase("GlobalDiscord")
                .getCollection<PongCounterEntry>("Pings")

            val searchQuery = and(
                PongCounterEntry::authorSnowflake eq pongEntry.authorSnowflake
            )

            // Update entry, if it doesn't exists — insert it
            // https://www.mongodb.com/docs/drivers/node/current/fundamentals/crud/write-operations/upsert/#performing-an-upsert
            collection.updateOne(searchQuery, pongEntry, UpdateOptions().upsert(true))
        }
    }
}