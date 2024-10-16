package com.yes.camera.data.repository

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.nio.ByteBuffer

class MediaEncoder : MediaCodec.Callback() {
    var codec: MediaCodec? = null
    var muxer: MediaMuxer? = null
    var trackIndex = -1
    var format: MediaFormat? = null

    val inputSurface = MediaCodec.createPersistentInputSurface()
    fun configure(
        width: Int, // ширина видео 4096,3072// 3840 x2160//max 1920x1080
        height: Int
    ): Surface {
        //  codec?.reset()
        getCodecs()
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = codecList.codecInfos
        /*  for (codecInfo in codecInfos) {
              if (codecInfo.isEncoder) {
                  val capabilities = codecInfo.getCapabilitiesForType("video/avc")
                  if (capabilities != null) {
                      val colorFormats = capabilities.colorFormats
                      if (colorFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)) {
                          Log.d("CodecSupport", "Codec supports COLOR_FormatSurface")
                      }
                      val bitrateRange = capabilities.videoCapabilities.bitrateRange
                      if (bitrateRange.contains(300000)) {
                          Log.d("CodecSupport", "Codec supports bitrate 300000")
                      }
                      val frameRateRange = capabilities.videoCapabilities.supportedFrameRates
                      if (frameRateRange.contains(30)) {
                          Log.d("CodecSupport", "Codec supports frame rate 30")
                      }
                  }
              }
          }*/

        val supported = isSupported(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val format = MediaFormat.createVideoFormat("video/avc", width, height)

        format.setInteger(MediaFormat.KEY_BIT_RATE, 300000) // битрейт видео в bps (бит в секунду)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 24)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)

        codec = MediaCodec.createEncoderByType("video/avc")
        codec?.setCallback(
            this, null
        )
        codec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec?.setInputSurface(inputSurface)
        // val inputSurface = codec!!.createInputSurface()
        /*  val surface=MediaCodec.createPersistentInputSurface()
            codec?.setInputSurface(surface)*/
        codec?.start()

        return inputSurface

    }

    var started = false
    fun start(file: File) {
        //  codec?.setInputSurface(inputSurface)

        muxer = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        muxer?.let {
            trackIndex = it.addTrack(format!!)
        }
        muxer?.start()
        started = true
        //  codec?.start()
    }

    fun stop() {
        /* muxer?.stop()
         muxer?.release()
         muxer = null*/
        //  codec?.signalEndOfInputStream()
        started = false
        muxer?.stop()
        muxer?.release()
        muxer = null

    }

    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        println()
    }
    private val _outputBuffer: MutableStateFlow<ByteArray?> = MutableStateFlow(null)
    val outputBuffer = _outputBuffer.asStateFlow()
    fun subscribeOutputBuffer(): StateFlow<ByteArray?> {
        return outputBuffer
    }
    override fun onOutputBufferAvailable(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo
    ) {
        val buffer = codec.getOutputBuffer(index)
        if (started) {
            buffer?.let { it ->
                _outputBuffer.update { it }
                muxer?.writeSampleData(trackIndex, it, info)
            }
        }
        codec.releaseOutputBuffer(index, false)
    }

    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        val t = e.diagnosticInfo

    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        this.format = format
        //  muxer?.let {
        //    trackIndex = it.addTrack(format)
        // it.start()
        //   }
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