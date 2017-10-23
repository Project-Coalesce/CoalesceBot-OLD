package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.`fun`.GifSequenceWriter
import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.GlobalCooldown
import com.coalesce.bot.tempDirectory
import com.coalesce.bot.utilities.readBytes
import net.dv8tion.jda.core.MessageBuilder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

@Command("Boi", "njsblessing")
@GlobalCooldown(20L)
class Boi {
    private val images = arrayOf("http://i.imgur.com/wBjEsAZ.jpg", "http://i.imgur.com/fhHuvIP.jpg", "http://i.imgur.com/k5BqbxH.jpg",
            "http://i.imgur.com/2VeEUTS.jpg", "http://i.imgur.com/hOYMcij.jpg", "http://i.imgur.com/Hx06UHz.jpg", "http://i.imgur.com/DpLS3ZV.jpg",
            "http://i.imgur.com/riXIKEq.jpg", "http://i.imgur.com/f7QugVt.jpg")

    @CommandAlias("Meme command (Boi.)")
    fun execute(context: CommandContext) {
        context("boi. ${images[ThreadLocalRandom.current().nextInt(images.size)]}", deleteAfter = 20L to TimeUnit.SECONDS)
    }
}

@Command("Kys", "suicide killyourself ferdzmiracle")
@GlobalCooldown(20L)
class Kys {
    private val images = arrayOf("http://i.imgur.com/wNHRydS.gif", "http://i.imgur.com/4HM0pIm.gifv", "https://media.tenor.co/images/f8c3b7aa341433e9eedca95e8ef9ca64/tenor.gif",
            "https://m.popkey.co/50dcd0/EjLDx_s-200x150.gif", "https://i.makeagif.com/media/9-06-2016/aItgFN.gif",
            "https://media.tenor.co/images/51302798cf651e8196578b362136ce86/tenor.gif")

    @CommandAlias("Meme command (Kill yourself)")
    fun execute(context: CommandContext) {
        context("<:feelsbad:302954104718884875> ${images[ThreadLocalRandom.current().nextInt(images.size)]}", deleteAfter = 20L to TimeUnit.SECONDS)
    }
}

@Command("Bork", "depcries")
@GlobalCooldown(20L)
class Bork {

    private val bar = ImageIO.read(javaClass.getResourceAsStream("/triggeredBar.png"))

    @CommandAlias("Meme command (Bork)")
    fun execute(context: CommandContext) {
        val frames = mutableListOf<BufferedImage>()
        val frameCount = 10
        val random = ThreadLocalRandom.current()

        val shakeAmount = 10
        val barShakeAmount = 7
        val frameSize = 368
        val image = ImageIO.read(ByteArrayInputStream(URL(context.author.effectiveAvatarUrl).openConnection().readBytes()))

        (0 .. frameCount).forEach {
            val frame = BufferedImage(frameSize, frameSize, BufferedImage.TYPE_INT_RGB)
            val g2d = frame.createGraphics()

            val (shakeX, shakeY) = (random.nextFloat() * shakeAmount).toInt() to (random.nextFloat() * shakeAmount).toInt()
            val (barShakeX, barShakeY) = (random.nextFloat() * barShakeAmount).toInt() to (random.nextFloat() * barShakeAmount).toInt()

            g2d.drawImage(image, -shakeX, -shakeY, frameSize + shakeAmount, frameSize + shakeAmount, null)
            g2d.drawImage(bar, -barShakeX, -barShakeY, frameSize + barShakeAmount, frameSize + barShakeAmount, null)

            frames.add(frame)
        }

        val file = File(tempDirectory, "tempGif_${System.currentTimeMillis()}.gif")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (file.exists()) file.delete()
        file.createNewFile()
        val writer = GifSequenceWriter(ImageIO.createImageOutputStream(file), BufferedImage.TYPE_INT_RGB,
                10, true)
        frames.forEach(writer::writeToSequence)
        writer.close()

        context.channel.sendFile(file, "triggered.gif", MessageBuilder().apply {
            appendln("Looks like ${context.author.asMention} is butthurt :(")
        }.build()).queue {
            file.delete()
        }
    }
}

@Command("Rules")
@GlobalCooldown(20L)
class Rules {
    @CommandAlias("Shows you rulezzzz")
    fun execute(context: CommandContext) {
        context("Psst, you... Head over to <#269178364483338250>. They got memes such as this one. " +
                "\nhttp://i.imgur.com/B50EQKp.png", deleteAfter = 20L to TimeUnit.SECONDS)
    }
}

@Command("EggsDee", "eggsd xd exd")
@GlobalCooldown(20L)
class EggsD {
    @CommandAlias("Meme command (Eggs d)")
    fun execute(context: CommandContext) {
        context(":egg: :egg: :regional_indicator_d: :regional_indicator_e:")
    }
}

@Command("Enough", "proxisreaction")
@GlobalCooldown(20L)
class Enough {
    @CommandAlias("Meme command (Enough is enough)")
    fun execute(context: CommandContext) {
        context("Enough is enough.\nhttp://i.imgur.com/KDdaUHW.jpg", deleteAfter = 30L to TimeUnit.SECONDS)
    }
}

@Command("Pi")
@GlobalCooldown(35L)
class Pi {
    @CommandAlias("Gives you way too much insight in pi, along with a dank meme.")
    fun pi(context: CommandContext) {
        context("π = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280" +
                "3482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294" +
                "8954930381964428810975665933446128475648233786783165271201909145648566923460348610454326648213393607260" +
                "2491412737245870066063155881748815209209628292540917153643678925903600113305305488204665213841469519415" +
                "1160943305727036575959195309218611738193261179310511854807446237996274956735188575272489122793818301194" +
                "9129833673362440656643086021394946395224737190702179860943702770539217176293176752384674818467669405132" +
                "0005681271452635608277857713427577896091736371787214684409012249534301465495853710507922796892589235420" +
                "19956112129021960864034418159813629774771309960518707211349999998372978049951059731732816096318595024459" +
                "4553469083026425223082533446850352619311881710100031378387528865875332083814206171776691473035982534904" +
                "2875546873115956286388235378759375195778185778053217122680661300192787661119590921642019893809525720106" +
                "5485863278865936153381827968230301952035301852968995773622599413891249721775283479131515574857242454150" +
                "6959508295331168617278558890750983817546374649393192550604009277016711390098488240128583616035637076601" +
                "0471018194295559619894676783744944825537977472684710404753464620804668425906949129331367702898915210475" +
                "2162056966024058038150193511253382430035587640247496473263914199272604269922796782354781636009341721641" +
                "2199245863150302861829745557067498385054945885869269956909272107975093029553211653449872027559602364806" +
                "6549911988183479775356636980742654252786255181841757467289097777279380008164706001614524919217321721477" +
                "2350141441973568548161361157352552133475741849468438523323907394143334547762416862518983569485562099219" +
                "2221842725502542568876717904946016534668049886272327917860857843838279679766814541009538837863609506800" +
                "642251252051173929848..." +
                "\nhttp://i.imgur.com/INtrkr2.png")
    }
}
