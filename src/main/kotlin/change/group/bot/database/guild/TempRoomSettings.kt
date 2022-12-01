package change.group.bot.database.guild

import change.group.bot.Config
import change.group.bot.models.TempRoomSettingsEntry
import com.mongodb.client.model.UpdateOptions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import org.litote.kmongo.*

class TempRoomSettings(private val guildID: Snowflake) {
    fun setSettings(newSettings: TempRoomSettingsEntry) {
        val db = KMongo.createClient(Config.mongoUrl)

        db.use {
            val collection = db
                .getDatabase("Guild-${guildID.value}")
                .getCollection<TempRoomSettingsEntry>("TempRoomSettings")

            val searchQuery = and(
                TempRoomSettingsEntry::creatorRoomId eq newSettings.creatorRoomId
            )

            // Update entry, if it doesn't exists — insert it
            collection.updateOne(searchQuery, newSettings, UpdateOptions().upsert(true))
        }
    }

    fun getSettings(): TempRoomSettingsEntry? {
        val db = KMongo.createClient(Config.mongoUrl)

        val result = db.use {
            val collection = db
                .getDatabase("Guild-${guildID.value}")
                .getCollection<TempRoomSettingsEntry>("TempRoomSettings")

            collection.findOne()
        }

        return result
    }
}

val Guild.tempRoomSettings: TempRoomSettings
    get() = TempRoomSettings(this.id)

val GuildBehavior.tempRoomSettings: TempRoomSettings
    get() = TempRoomSettings(this.id)