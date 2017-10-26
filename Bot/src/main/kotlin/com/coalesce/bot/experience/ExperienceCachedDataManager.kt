package com.coalesce.bot.experience

import com.coalesce.bot.CoCoinsValue
import com.coalesce.bot.experienceFile
import com.coalesce.bot.gson
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Font
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ExperienceCachedDataManager {

    val experience = mutableMapOf<Long, Pair<Int, Int>>()
    init {
        if (!experienceFile.parentFile.exists()) experienceFile.parentFile.mkdirs()
        if (!experienceFile.exists()) {
            experienceFile.apply {
                createNewFile()
                experienceFile.writeText(gson.toJson(experience))
            }
        } else {
            experience.putAll(gson.fromJson(experienceFile.readText(), experience.javaClass))
        }
    }

    /* IMAGE STUFF */
    private val imageBackground = ImageIO.read(javaClass.getResourceAsStream("/levelupbackground.png"))
    private val imageStar = ImageIO.read(javaClass.getResourceAsStream("/levelupstar.png"))
    private val imageFont = Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/8-Bit Madness.ttf")).deriveFont(Font.PLAIN, 42F)

    /* VALUE STUFF */
    private val exponent = 1.04
    private val baseXp = 30

    /* BLAH BLAH BLAH */
    fun expAdd(user: User, amount: Int, wrappedUser: CoCoinsValue, channel: TextChannel) {

        /*val exp = getExp(user)
        println("EXP: $exp")
        val level = getLevel(exp)
        println("EXP IN LEVEL: ${getExpInLevel(level)}")
        println("EXP TO NEXT LEVEL: ${getExpToLevel(level)}")


        if (getExpToLevel(level) <= 0) {
            channel.sendFile(ByteArrayOutputStream().apply { ImageIO.write(generateLevelUpImage(level,
                    ImageIO.read(ByteArrayInputStream(URL(user.effectiveAvatarUrl).openConnection().readBytes()))), "png",
                    this) }.toByteArray(), "levelUp_${user.idLong}_${System.currentTimeMillis()}.png", MessageBuilder().apply {
                        appendln(user.asMention + ": LEVEL UP!")
                    }.build()).queue()
            wrappedUser.transaction(CoCoinsTransaction("Reward for Leveling Up!", 3.0), channel, user)
        }
        save(user.idLong, this[user.idLong] + amount)*/
    }

    private fun generateLevelUpImage(level: Int, avatar: Image): BufferedImage {
        val image = BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.font = imageFont
        val metrics = graphics.getFontMetrics(imageFont)

        graphics.drawImage(imageBackground, 0, 0, 250, 250, null)
        graphics.drawImage(avatar, 41, 21, 171, 171, null)
        graphics.drawImage(imageStar, 73, 120, 110, 110, null)

        graphics.drawString(level.toString(), ((250 - metrics.stringWidth(level.toString())) / 2), 185)
        graphics.drawString("LEVEL UP!", ((250 - metrics.stringWidth("LEVEL UP!")) / 2), 25)

        graphics.dispose()

        return image
    }
}
