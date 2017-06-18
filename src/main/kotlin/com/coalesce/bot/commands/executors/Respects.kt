package com.coalesce.bot.commands.executors

import com.coalesce.bot.*
import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.binary.LongSerializer
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.ifwithDo
import com.coalesce.bot.utilities.limit
import com.coalesce.bot.utilities.parseDouble
import com.coalesce.bot.utilities.quietly
import com.google.gson.reflect.TypeToken
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.awt.Color
import java.io.DataOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class RespectReactions(val message: String,
                            val amount: Double,
                            val delay: Double,
                            val rating: String,
                            val emoteName: Optional<String> = Optional.empty(),
                            val emoteId: Optional<Long> = Optional.empty()) {
    NOT_DANK_ENOUGH("Not Dank Enough", -1.0, 1260.0, "0/10", emoteId = Optional.of(304043388523511808L)),
    FUNNY("Funny 🥚🥚🇩 🇪", 1.0, 860.0, "6.9/10", emoteName = Optional.of("😂")),
    LIT("Lit Fam", 2.0, 720.0, "8.5/10", emoteName = Optional.of("🔥")),
    DANK("Dank", 3.0, 1260.0, "10/10", emoteId = Optional.of(318557118791680000L))
}

class Respects @Inject constructor(val bot: Main): Embeddables, Runnable {
    private val resetTime = 14L to TimeUnit.DAYS
    private val resetTimeMillis = TimeUnit.MILLISECONDS.convert(resetTime.first, resetTime.second)

    init {
        val time = if (respectsResetFile.exists()) System.currentTimeMillis() - LongSerializer(respectsResetFile).read() else run {
            LongSerializer(respectsResetFile).write(System.currentTimeMillis() + resetTimeMillis)
            resetTimeMillis
        }
        if (time < 0) {
            this.run()
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, resetTimeMillis, resetTimeMillis,
                    TimeUnit.MILLISECONDS)
        } else {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, time, resetTimeMillis,
                    TimeUnit.MILLISECONDS)
        }
    }

    // Used to give the memelord role every respects reset time.
    override fun run() {
        val guild = bot.jda.getGuildById(268187052753944576L) ?: return

        respectsResetFile.delete()
        val map = RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()
        val newMap = mutableMapOf<String, Double>()
        respectsLeaderboardsFile.delete()

        var top3 = mutableListOf<Member>()
        val amountPositions = mutableListOf<Double>()

        map.forEach { key, value ->
            quietly {
                val member = guild.getMember(bot.jda.getUserById(key)) ?: return@quietly
                if (value is Double) {
                    top3.add(member)
                    amountPositions.add(value)
                }
            }
        }

        Collections.sort(amountPositions)
        Collections.reverse(amountPositions)
        top3 = top3.subList(0, Math.min(top3.size, 3))
        Collections.sort(top3, { second, first -> (map[first.user.id] as Double).toInt() - (map[second.user.id] as Double).toInt() })
        if (top3.size > 3) {
            val back = mutableListOf<Member>()
            back.addAll(top3.subList(0, 3))
            top3.clear()
            top3.addAll(back)
        }

        guild.publicChannel.sendMessage(StringBuilder("**The respects reset period was reached!**\nResults:\n").apply {
            top3.forEach {
                val pos = amountPositions.indexOf(map[it.user.id] as Double)
                if (pos == 1) {
                    append("**Congratulations, ${it.user.asMention}! You reached the first place!** " +
                            "As a reward, you will be given the **MemeLord** role and a 4 respects boost on the new leaderboard.\n")
                    newMap[it.user.id] = 4.0
                    guild.controller.addRolesToMember(guild.getMember(it.user), guild.getRoleById(325006830332018688L)).queue()
                } else {
                    append("${it.user}: You'll get $pos respects on the new leaderboard for being in the the position $pos.")
                    newMap[it.user.id] = pos.toDouble()
                }
            }
        }.toString()).queue()

        RespectsLeaderboardSerializer(respectsLeaderboardsFile).write(newMap)
    }

    @RootCommand(
            name = "Respects",
            aliases = arrayOf("f", "nahusdream"),
            description = "Over-engineered meme command (Press F to pay respects)",
            permission = "commands.respects",
            type = CommandType.FUN,
            globalCooldown = 1800.0,
            userCooldown = 4.0 * 3600.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "Respects have been paid! **+4 respect**") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(60, TimeUnit.SECONDS) } }

        transaction(context.author, 4.0, context.channel)
    }

    @JDAListener
    fun message(event: MessageReceivedEvent, context: EventContext) {
        if (!event.message.attachments.isEmpty() && event.channel.idLong == 308791021343473675L && !event.message.author.isBot) {
            RespectReactions.values().forEach {
                if (it.emoteName.isPresent) {
                    event.message.addReaction(it.emoteName.get()).queue()
                } else quietly {
                    event.message.addReaction(event.guild.getEmoteById(it.emoteId.get())).queue()
                }
            }
        }
    }

    @JDAListener
    fun react(event: MessageReactionAddEvent, context: EventContext) {
        if (event.user.isBot || bot.listener.isBlacklisted(event.user)) return //Bad boys can't do this

        if (event.channel.idLong == 308791021343473675L/* #memes */) {
            RespectReactions.values().forEach {
                if (it.emoteName.isPresent && event.reaction.emote.name == it.emoteName.get() && context.runChecks(event.user, event.channel!!, it.delay, it.name)) {
                    event.channel.getMessageById(event.messageId).queue { message ->
                        dank(event.channel!!, event.user, message.author, event.jda, it)
                    }
                    return
                }
            }

            if (event.reaction.guild != null && !event.reaction.emote.emote.isManaged) {
                RespectReactions.values().forEach {
                    if (it.emoteId.isPresent && it.emoteId.get() == event.reaction.emote.idLong && context.runChecks(event.user, event.channel!!, it.delay, it.name)) {
                        event.channel.getMessageById(event.messageId).queue { message ->
                            dank(event.channel!!, event.user, message.author, event.jda, it)
                        }
                        return
                    }
                }
            }
        }
    }

    private fun dank(channel: MessageChannel, from: User, to: User, jda: JDA, reaction: RespectReactions) {
        if (to == from || to == jda.selfUser) {
            channel.sendMessage("Invalid user").queue()
            return
        }
        transaction(to, reaction.amount, channel)
        channel.sendMessage("${to.asMention}: Meme rating from ${from.name}: \"${reaction.message}\" - ${reaction.rating} " +
                "**${if (reaction.amount > 0) "+" else ""}${reaction.amount.toInt()} respect**").queue()
    }

    @SubCommand(
            name = "reset",
            permission = "commands.respects.reset",
            globalCooldown = 0.0
    )
    fun resetScore(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            throw ArgsException("You need to mention someone to reset scores of.")
        }
        val user = context.message.mentionedUsers.first()
        val file = respectsLeaderboardsFile
        if (!file.exists()) generateFile(file)

        val serializer = RespectsLeaderboardSerializer(file)
        val map = serializer.read()

        if (!map.containsKey(user.id)) {
            throw ArgsException("This user already is empty.")
        }

        map.remove(user.id)
        serializer.write(map)
        context(context.author, "Reset scores of ${user.asMention}.")
    }

    @SubCommand(
            name = "edit",
            permission = "commands.respects.edit",
            globalCooldown = 0.0
    )
    fun scoreEdit(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty() || context.args.size < 2) {
            throw ArgsException("Usage: `!f edit <mention> <amount>`")
        }
        val user = context.message.mentionedUsers.first()

        val file = respectsLeaderboardsFile
        if (!file.exists()) generateFile(file)

        val amount = context.args[1].parseDouble() ?: run {
            throw ArgsException("Amount specified '${context.args[1]}' is not a valid value.")
        }

        setRespects(user, context.channel, {
            val result = it + amount
            context(context.author, "Set scores of ${user.asMention} to $result.")
            result
        })
    }

    @SubCommand(
            name = "set",
            permission = "commands.respects.set",
            globalCooldown = 0.0
    )
    fun scoreSet(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty() || context.args.size < 2) {
            throw ArgsException("Usage: `!f set <mention> <amount>`")
        }
        val user = context.message.mentionedUsers.first()

        val file = respectsLeaderboardsFile
        if (!file.exists()) generateFile(file)

        val amount = context.args[1].parseDouble() ?: run {
            throw ArgsException("Amount specified '${context.args[1]}' is not a valid value.")
        }
        setRespects(user, context.channel, { amount })
        context(context.author, "Set scores of ${user.asMention} to $amount.")
    }

    @SubCommand(
            name = "leaderboard",
            aliases = arrayOf("fboard", "lboard", "board", "respectsboard", "rboard", "ftop", "top"),
            permission = "commands.respects.leaderboard",
            globalCooldown = 30.0
    )
    fun fboard(context: SubCommandContext) {
        val file = respectsLeaderboardsFile
        synchronized(file) {
            val serializer = RespectsLeaderboardSerializer(file)
            val map = serializer.read()

            var top10 = mutableListOf<Member>()
            val amountPositions = mutableListOf<Double>()

            map.forEach { key, value ->
                quietly {
                    val member = context.message.guild.getMember(bot.jda.getUserById(key)) ?: return@quietly
                    if (value is Double) {
                        top10.add(member)
                        amountPositions.add(value)
                    }
                }
            }

            Collections.sort(amountPositions)
            Collections.reverse(amountPositions)
            top10 = top10.subList(0, Math.min(top10.size, 10))
            Collections.sort(top10, { second, first -> (map[first.user.id] as Double).toInt() - (map[second.user.id] as Double).toInt() })
            if (top10.size > 10) {
                val back = mutableListOf<Member>()
                back.addAll(top10.subList(0, 10))
                top10.clear()
                top10.addAll(back)
            }

            val positionStr = StringBuilder()
            val nameStr = StringBuilder()
            val respectsPaidStr = StringBuilder()

            top10.forEach {
                val value = map[it.user.id] as Double

                positionStr.append("#${amountPositions.indexOf(value) + 1}\n")
                nameStr.append("${(it.effectiveName).limit(16)}\n")
                respectsPaidStr.append("${value.toInt()}\n")
            }

            val member = context.message.member
            if(map.containsKey(member.user.id) && !top10.contains(member)) {
                val value = map[member.user.id] as Double

                positionStr.append("...\n#${amountPositions.indexOf(value) + 1}")
                nameStr.append("...\n${(member.effectiveName).limit(16)}")
                respectsPaidStr.append("...\n${value.toInt()}")
            }

            context(embed().apply {
                setColor(Color(0x5ea81e))

                addField("Position", positionStr.toString(), true)
                addField("Name", nameStr.toString(), true)
                addField("Respects", respectsPaidStr.toString(), true)
            })
        }
    }

    private fun transaction(user: User, amount: Double, channel: MessageChannel) {
        val file = respectsLeaderboardsFile
        if (!file.exists()) generateFile(file)
        setRespects(user, channel, { it + amount })
    }

    private fun generateFile(file: File) {
        file.createNewFile()
        if (respectsLeaderboardsFileOld.exists()) {
            val oldMap = gson.fromJson<MutableMap<String, Double>>(respectsLeaderboardsFileOld.readText(), object: TypeToken<HashMap<String, Double>>() {}.type)

            val repSerializer = RespectsLeaderboardSerializer(file)
            repSerializer.write(oldMap)

            val oldSize = respectsLeaderboardsFileOld.length()
            respectsLeaderboardsFileOld.delete()
            println("Updated reputation file to binary, removing ${oldSize - file.length()} bytes.")
        } else {
            file.outputStream().use {
                DataOutputStream(it).writeLong(-1L)
            }
        }
    }
}

fun setRespects(user: User, channel: MessageChannel, processAmount: (Double) -> Double,
                serializer: RespectsLeaderboardSerializer = RespectsLeaderboardSerializer(respectsLeaderboardsFile),
                map: MutableMap<String, Double> = serializer.read()) {
    val old = map[user.id] ?: 0.0
    val value = processAmount(old)
    map[user.id] = value
    serializer.write(map)

    if (map.none { it.value > value } && map.any { it.value >= old }) {
        channel.sendMessage("${user.asMention} has reached first place in the respects leaderboard!").queue()
    }
}
