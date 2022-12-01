package change.group.bot.database.guild

import change.group.bot.Config
import change.group.bot.models.TempRoomEntry
import com.mongodb.client.model.UpdateOptions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import org.bson.conversions.Bson
import org.litote.kmongo.*

class TempRoomCollection(private val guildID: Snowflake) {
    fun updateRoom(newRoomSettings: TempRoomEntry) {
        val db = KMongo.createClient(Config.mongoUrl)

        db.use {
            val collection = db
                .getDatabase("Guild-${guildID.value}")
                .getCollection<TempRoomEntry>("TempRoomCollection")

            val searchQuery = and(
                TempRoomEntry::authorId eq newRoomSettings.authorId
            )

            // Update entry, if it doesn't exists — insert it
            collection.updateOne(searchQuery, newRoomSettings, UpdateOptions().upsert(true))
        }
    }

    fun getRoom(query: Bson): TempRoomEntry? {
        val db = KMongo.createClient(Config.mongoUrl)

        val result = db.use {
            val collection = db
                .getDatabase("Guild-${guildID.value}")
                .getCollection<TempRoomEntry>("TempRoomCollection")

            val searchQuery = and(query)
            collection.findOne(searchQuery)
        }

        return result
    }

    fun getRoomByAuthor(authorId: String): TempRoomEntry? {
        return getRoom(TempRoomEntry::authorId eq authorId)
    }
    fun getRoomByRoomId(roomId: String): TempRoomEntry? {
        return getRoom(TempRoomEntry::previousRoomId eq roomId)
    }
}

val Guild.tempRoomCollection: TempRoomCollection
    get() = TempRoomCollection(this.id)

val GuildBehavior.tempRoomCollection: TempRoomCollection
    get() = TempRoomCollection(this.id)