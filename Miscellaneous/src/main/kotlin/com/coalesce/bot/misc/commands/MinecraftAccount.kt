package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.awt.Color
import java.io.IOException
import java.net.URL
import java.util.*

@Command("MinecraftAccount", "minecraft mc mienkreft")
class MinecraftAccount: Embeddables {
    private val hypixelAPIKey = "fb27c78f-fc08-421f-a91a-501a905309bc"
    private val mojangAPI = MojangAPIWrapper()

    @CommandAlias("See info about a minecraft account by name")
    fun execute(context: CommandContext, name: String) = execute(context, mojangAPI.findUUID(name))

    @CommandAlias("See info about a minecraft account by UUID")
    fun execute(context: CommandContext, uuid: UUID) {
        val nameHist = mojangAPI.nameHistory(uuid).joinToString(separator = "\n") { "**${it.name}** (${if(it.changedToAt == 0L)
                "Original" else "Changed at ${it.changedToAt.formatTime()}"})" }
        val hypixelInfo = tryOrNull { gson.fromJson(URL("https://api.hypixel.net/player?key=$hypixelAPIKey&uuid=$uuid").openConnection().readText(), HypixelPlayerRequest::class.java) }
        val hiveMCInfo = /*tryOrNull {*/ gson.fromJson(URL("http://api.hivemc.com/v1/player/$uuid").openConnection().readText(), HiveMCPlayer::class.java)// }

        context(embed().apply {
            embColor = Color(112, 255, 45)
            embTitle = "Minecraft profile (UUID $uuid)"
            embDescription = nameHist
            setThumbnail("https://crafatar.com/renders/body/$uuid")
            if (hypixelInfo?.player != null) {
                field("Hypixel", StringBuilder().apply {
                    val player = hypixelInfo.player
                    appendln("First login at ${player.firstLogin.formatTime()}, last login at ${player.lastLogin.formatTime()}, " +
                            "Minecraft version ${player.mcVersionRp}, ${player.timePlaying} hours played.")
                    appendln("Rank: ${player.packageRank}, level ${player.networkLevel}.")
                    appendln("**Game Statistics (Not all of them, type `!mc hypixel <name>` for that)**")
                    val quake = player.stats.quake
                    appendln("**Quake** " +
                            "K/D Ratio: ${(quake["kills"].asInt.toDouble() / quake["deaths"].asInt.toDouble()).round(3)}, " +
                            "Wins: ${quake["wins"].asInt}")
                    val skywars = player.stats.skywars
                    appendln("**SkyWars** " +
                            "K/D Ratio: ${(skywars["kills"].asInt.toDouble() / skywars["deaths"].asInt.toDouble()).round(3)}, " +
                            "Wins: ${skywars["wins"].asInt}, " +
                            "Win Streak: ${skywars["win_streak"].asInt}")
                    val bedwars = player.stats.bedwars
                    appendln("**BedWars** " +
                            "K/D Ratio: ${(bedwars["kills_bedwars"].asInt.toDouble() / bedwars["deaths_bedwars"].asInt.toDouble()).round(3)}, " +
                            "W/L Ratio: ${(bedwars["wins_bedwars"].asInt.toDouble() / bedwars["games_played_bedwars"].asInt.toDouble()).round(3)}, " +
                            "Beds :b:roken: ${bedwars["beds_broken_bedwars"].asInt}")
                }.toString(), false)
            }
            if (hiveMCInfo != null) {
                field("HiveMC", StringBuilder().apply {
                    appendln("First login at ${hiveMCInfo.firstLogin.formatTime()}, last login at ${hiveMCInfo.lastLogin.formatTime()}.")
                    appendln("Tokens: ${hiveMCInfo.tokens}, Credits: ${hiveMCInfo.credits}, Medals: ${hiveMCInfo.medals}, Rank: ${hiveMCInfo.rankName}.")
                    appendln("${hiveMCInfo.status.description} ${hiveMCInfo.status.game}")

                    fun getGameStats(type: String) = gson.fromJson(URL("http://api.hivemc.com/v1/player/$uuid/$type").openConnection().readText(),
                            JsonObject::class.java)

                    appendln("**Game Statistics (Not all of them, type `!mc hivemc <name>` for that)**")
                    val skywars = getGameStats("SKY")
                    appendln("**Skywars** " +
                            "K/D Ratio: ${(skywars["kills"].asInt.toDouble() / skywars["deaths"].asInt.toDouble()).round(3)}, " +
                            "Wins: ${skywars["wins"].asInt}, " +
                            "Time Alive: ${skywars["timealive"].asInt.toLong().formatTimeDiff()}")
                    val heroesSG = getGameStats("HERO")
                    appendln("**Heroes** " +
                            "K/D Ratio: ${(heroesSG["kills"].asInt.toDouble() / heroesSG["deaths"].asInt.toDouble()).round(3)}, " +
                            "W/L Ratio: ${(heroesSG["victories"].asInt.toDouble() / heroesSG["deaths"].asInt.toDouble()).round(3)}")
                    val skyGiants = getGameStats("GNT")
                    appendln("**Sky Giants** " +
                            "K/D Ratio: ${(skyGiants["kills"].asInt.toDouble() / skyGiants["deaths"].asInt.toDouble()).round(3)}, " +
                            "W/L Ratio: ${(skyGiants["victories"].asInt.toDouble() / skyGiants["deaths"].asInt.toDouble()).round(3)}, " +
                            "Giants killed: ${skyGiants["beasts_slain"].asInt}")
                }.toString(), false)
            }
        })
    }

    @SubCommand("APIStatus", "status mojangapistatus check", "View Mojang API Status")
    fun apiStatus(context: CommandContext) {
    }

}

data class HiveMCPlayer(
    val rankName: String,
    val tokens: Int,
    val credits: Int,
    val medals: Int,
    val status: Status,
    val firstLogin: Long,
    val lastLogin: Long
) {
    class Status(
        val description: String,
        val game: String
    )
}

data class HypixelPlayerRequest(val player: HypixelPlayer?) {
    data class HypixelPlayer(
            val stats: Stats,
            val timePlaying: Int,
            val networkLevel: Int,
            val firstLogin: Long,
            val lastLogin: Long,
            val packageRank: String,
            val mcVersionRp: String
    ) {
        data class Stats(
            @SerializedName("Arcade") val arcade: JsonObject,
            @SerializedName("Arena") val arena: JsonObject,
            @SerializedName("HungerGames") val blitzSG: JsonObject,
            @SerializedName("MCGO") val mcgo: JsonObject,
            @SerializedName("Paintball") val paintball: JsonObject,
            @SerializedName("Quake") val quake: JsonObject,
            @SerializedName("TNTGames") val tntGames: JsonObject,
            @SerializedName("VampireZ") val vampireZ: JsonObject,
            @SerializedName("Walls") val walls: JsonObject,
            @SerializedName("Walls3") val megaWalls: JsonObject,
            @SerializedName("Battleground") val warlords: JsonObject,
            @SerializedName("SkyWars") val skywars: JsonObject,
            @SerializedName("UHC") val uhc: JsonObject,
            @SerializedName("TrueCombat") val crazyWalls: JsonObject,
            @SerializedName("SuperSmash") val smashHeroes: JsonObject,
            @SerializedName("SpeedUHC") val speedUHC: JsonObject,
            @SerializedName("Bedwars") val bedwars: JsonObject
        )
    }
}

class MojangAPIWrapper {
    fun findUUID(name: String): UUID {
        try {
            return UUID.fromString(gson.fromJson(URL("https://api.mojang.com/users/profiles/minecraft/$name")
                        .openConnection().getInputStream().readText(), JsonObject::class.java)["id"].asString.insertDashUUID())
        } catch (ioex: IOException) {
            throw ArgsException("That name is not being used!")
        }
    }

    fun nameHistory(uuid: UUID) = gson.fromJson<List<Name>>(URL("https://api.mojang.com/user/profiles/${uuid.toString().replace("-", "")}" +
            "/names").openConnection().getInputStream().readText(), object: TypeToken<List<Name>>() {}.type)!!
}

data class Name(val name: String, val changedToAt: Long = 0L)