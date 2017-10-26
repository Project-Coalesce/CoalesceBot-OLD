package com.coalesce.bot.`fun`

import java.awt.image.RenderedImage
import javax.imageio.*
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageOutputStream


class GifSequenceWriter
/**
 * Creates a new GifSequenceWriter

 * @param outputStream the ImageOutputStream to be written to
 * *
 * @param imageType one of the imageTypes specified in BufferedImage
 * *
 * @param timeBetweenFramesMS the time between frames in miliseconds
 * *
 * @param loopContinuously wether the gif should loop repeatedly
 * *
 * @throws IIOException if no gif ImageWriters are found
 * *
 * *
 * @author Elliot Kroo (elliot[at]kroo[dot]net)
 */
constructor(outputStream: ImageOutputStream, imageType: Int, timeBetweenFramesMS: Int, loopContinuously: Boolean) {
    var gifWriter: ImageWriter
    var imageWriteParam: ImageWriteParam
    var imageMetaData: IIOMetadata

    init {
        // my method to create a writer
        gifWriter = writer
        imageWriteParam = gifWriter.defaultWriteParam
        val imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType)

        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
                imageWriteParam)

        val metaFormatName = imageMetaData.nativeMetadataFormatName

        val root = imageMetaData.getAsTree(metaFormatName) as IIOMetadataNode

        val graphicsControlExtensionNode = getNode(root, "GraphicControlExtension")

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none")
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE")
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE")
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10))
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0")

        val commentsNode = getNode(root, "CommentExtensions")
        commentsNode.setAttribute("CommentExtension", "Created by MAH")

        val appEntensionsNode = getNode(
                root,
                "ApplicationExtensions")

        val child = IIOMetadataNode("ApplicationExtension")

        child.setAttribute("applicationID", "NETSCAPE")
        child.setAttribute("authenticationCode", "2.0")

        val loop = if (loopContinuously) 0 else 1

        child.userObject = byteArrayOf(0x1, (loop and 0xFF).toByte(), (loop shr 8 and 0xFF).toByte())
        appEntensionsNode.appendChild(child)

        imageMetaData.setFromTree(metaFormatName, root)

        gifWriter.output = outputStream

        gifWriter.prepareWriteSequence(
                null)
    }

    fun writeToSequence(img: RenderedImage) {
        gifWriter.writeToSequence(
                IIOImage(
                        img, null,
                        imageMetaData),
                imageWriteParam)
    }

    /**
     * Close this GifSequenceWriter object. This does not close the underlying
     * stream, just finishes off the GIF.
     */
    fun close() {
        gifWriter.endWriteSequence()
    }

    companion object {

        /**
         * Returns the first available GIF ImageWriter using
         * ImageIO.getImageWritersBySuffix("gif").

         * @return a GIF ImageWriter object
         * *
         * @throws IIOException if no GIF image writers are returned
         */
        private val writer: ImageWriter
            get() {
                val iter = ImageIO.getImageWritersBySuffix("gif")
                if (!iter.hasNext()) {
                    throw IIOException("No GIF Image Writers Exist")
                } else {
                    return iter.next()
                }
            }

        /**
         * Returns an existing child node, or creates and returns a new child node (if
         * the requested node does not exist).

         * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
         * *
         * @param nodeName the name of the child node.
         * *
         * *
         * @return the child node, if found or a new node created with the given name.
         */
        private fun getNode(
                rootNode: IIOMetadataNode,
                nodeName: String): IIOMetadataNode {
            val nNodes = rootNode.length
            for (i in 0 .. nNodes - 1) {
                if (rootNode.item(i).nodeName.compareTo(nodeName, ignoreCase = true) == 0) {
                    return rootNode.item(i) as IIOMetadataNode
                }
            }
            val node = IIOMetadataNode(nodeName)
            rootNode.appendChild(node)
            return node
        }
    }
}

