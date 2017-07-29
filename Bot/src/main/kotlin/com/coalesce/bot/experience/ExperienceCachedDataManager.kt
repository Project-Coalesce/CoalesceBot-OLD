package com.coalesce.bot.experience

import com.coalesce.bot.CachedDataManager
import com.coalesce.bot.CoCoinsTransaction
import com.coalesce.bot.CoCoinsValue
import com.coalesce.bot.binary.ExperienceSerializer
import com.coalesce.bot.experienceFile
import com.coalesce.bot.utilities.readBytes
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Font
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

class ExperienceCachedDataManager: CachedDataManager<Long, Int>(experienceFile, ExperienceSerializer(experienceFile), { 0 }) {
    private val imageBackground = ImageIO.read(javaClass.getResourceAsStream("/levelupbackground.png"))
    private val imageStar = ImageIO.read(javaClass.getResourceAsStream("/levelupstar.png"))
    private val imageFont = Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/8-Bit Madness.ttf"))

    fun expAdd(user: User, amount: Int, wrappedUser: CoCoinsValue, channel: TextChannel) {
        val xp = this[user.idLong]
        val level = ((xp - 30) / 33.5).toInt()
        val nextAchievement = 30 + (level + 1) * 60
        if (this[user.idLong] + amount >= nextAchievement) {
            channel.sendFile(ByteArrayOutputStream().apply { ImageIO.write(generateLevelUpImage(level + 1,
                    ImageIO.read(ByteArrayInputStream(URL(user.effectiveAvatarUrl).openConnection().readBytes()))), "jpg",
                    this) }.toByteArray(), "levelUp", MessageBuilder().apply {
                        appendln(user.asMention + ": LEVEL UP!")
                        ExperienceRewards.checkRewards(level + 1).forEach {
                            appendln(it.handleAchieving(channel, user))
                        }
                    }.build())
            wrappedUser.transaction(CoCoinsTransaction("Reward for level up", 3.0), channel, user)
        }
        save(user.idLong, this[user.idLong] + amount)
    }

    private fun generateLevelUpImage(level: Int, avatar: Image): BufferedImage {
        val image = BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        graphics.font = imageFont
        val metrics = graphics.getFontMetrics(imageFont)

        graphics.drawImage(imageBackground, 0, 0, 250, 250, null)
        graphics.drawImage(avatar, 41, 21, 171, 171, null)
        graphics.drawImage(imageStar, 73, 120, 110, 110, null)

        graphics.drawString(level.toString(), 250 / 2 + metrics.stringWidth(level.toString()) / 2, 168)
        graphics.drawString("LEVEL UP!", 250 / 2 + metrics.stringWidth("LEVEL UP!") / 2, 15)

        graphics.dispose()

        return image
    }
}
