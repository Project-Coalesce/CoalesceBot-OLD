package com.coalesce.bot.`fun`

import javax.imageio.*
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageOutputStream
import java.awt.image.RenderedImage
import java.io.IOException

class GifSequenceWriter constructor(outputStream: ImageOutputStream, imageType: Int, timeBetweenFramesMS: Int, loopContinuously: Boolean) {
    val gifWriter: ImageWriter
    val imageWriteParam: ImageWriteParam
    val imageMetaData: IIOMetadata

    init {
        gifWriter = writer
        imageWriteParam = gifWriter.defaultWriteParam
        val imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType)

        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam)

        val metaFormatName = imageMetaData.nativeMetadataFormatName
        val root = imageMetaData.getAsTree(metaFormatName) as IIOMetadataNode
        val graphicsControlExtensionNode = getNode(root, "GraphicControlExtension")

        graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor")
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE")
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE")
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10))
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0")

        val commentsNode = getNode(root, "CommentExtensions")
        commentsNode.setAttribute("CommentExtension", "CoalesceBot Generated :)")

        val appEntensionsNode = getNode(root, "ApplicationExtensions")

        val child = IIOMetadataNode("ApplicationExtension")

        child.setAttribute("applicationID", "NETSCAPE")
        child.setAttribute("authenticationCode", "2.0")

        val loop = if (loopContinuously) 0 else 1

        child.userObject = byteArrayOf(0x1, (loop and 0xFF).toByte(), (loop shr 8 and 0xFF).toByte())
        appEntensionsNode.appendChild(child)

        imageMetaData.setFromTree(metaFormatName, root)

        gifWriter.output = outputStream
        gifWriter.prepareWriteSequence(null)
    }

    fun writeToSequence(img: RenderedImage) {
        gifWriter.writeToSequence(IIOImage(img, null, imageMetaData), imageWriteParam)
    }

    fun close() {
        gifWriter.endWriteSequence()
    }

    private val writer: ImageWriter
        get() {
            val iter = ImageIO.getImageWritersBySuffix("gif")
            if (!iter.hasNext()) {
                throw IIOException("No GIF Image Writers Exist")
            } else {
                return iter.next()
            }
        }

    private fun getNode(rootNode: IIOMetadataNode, nodeName: String): IIOMetadataNode {
        val nNodes = rootNode.length
        (0 .. nNodes - 1).forEach {
            if (rootNode.item(it).nodeName.compareTo(nodeName, ignoreCase = true) == 0) {
                return rootNode.item(it) as IIOMetadataNode
            }
        }

        val node = IIOMetadataNode(nodeName)
        rootNode.appendChild(node)

        return node
    }
}
