package com.yes.camera.data.repository

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

class MediaEncoder: MediaCodec.Callback() {
    var muxer: MediaMuxer?=null
    var trackIndex = -1
    fun prepareMediaCodec(file: File): MediaCodec {
        getCodecs()
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = codecList.codecInfos
        val supported = isSupported(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val file = createFile("mp4")

        //   val  outputStream = BufferedOutputStream(FileOutputStream(mFile))


        val width = 640 // ширина видео 4096,3072// 3840 x2160//max 1920x1080
        val height =480



        val format = MediaFormat.createVideoFormat("video/avc", width, height)

        format.setInteger(MediaFormat.KEY_BIT_RATE, 3000000) // битрейт видео в bps (бит в секунду)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)

        val  codec = MediaCodec.createEncoderByType("video/avc")
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        //  mEncoderSurface = codec.createInputSurface()
        val newFormat: MediaFormat = codec.outputFormat

        val muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)


        trackIndex = muxer?.addTrack(newFormat)!!
        //  muxer?.start()
        codec.setCallback(
            this
        )

        return codec
    }


    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        TODO("Not yet implemented")
    }

    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        val encoderOutputBuffers = codec.getOutputBuffer(index)
        val data = ByteBuffer.allocate(info.size)
        encoderOutputBuffers?.get(data.array())

        // outputStream.write(outDate, 0, outDate.size) // гоним байты в поток

        muxer?.writeSampleData(trackIndex, data,info)

        codec.releaseOutputBuffer(index, false)
    }

    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        TODO("Not yet implemented")
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        TODO("Not yet implemented")
    }

    private fun getCodecs() {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecs = codecList.codecInfos.filter { it.isEncoder }
        for (codec in codecs) {
            val mimeTypes = codec.supportedTypes
            for (mimeType in mimeTypes) {
                if (mimeType.startsWith("video/")) {
                    val (maxWidth, maxHeight) = getMaxResolution(codec, mimeType)
                    println("Codec: ${codec.name}, MIME Type: $mimeType, Max Resolution: ${maxWidth}x${maxHeight}")
                } else {
                    println("Codec: ${codec.name}, MIME Type: $mimeType")
                }
            }
        }
    }
    private fun getMaxResolution(codecInfo: MediaCodecInfo, mimeType: String): Pair<Int, Int> {
        val capabilities = codecInfo.getCapabilitiesForType(mimeType)

        val videoCapabilities = capabilities.videoCapabilities
        val maxWidth = videoCapabilities.supportedWidths.upper
        val maxHeight = videoCapabilities.supportedHeights.upper
        return Pair(maxWidth, maxHeight)
    }


    private fun isCodecSupported(mimeType: String?): Boolean {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = codecList.codecInfos
        for (codecInfo in codecInfos) {
            if (!codecInfo.isEncoder) {
                continue
            }
            val supportedTypes = codecInfo.supportedTypes
            for (type in supportedTypes) {
                if (type.equals(mimeType, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isSupported(type: String): Boolean {
        val isOutputFormatSupported = isCodecSupported(type)
        //  val isVideoEncoderSupported = isCodecSupported("video/avc")
        return isOutputFormatSupported //&& isVideoEncoderSupported
    }
}