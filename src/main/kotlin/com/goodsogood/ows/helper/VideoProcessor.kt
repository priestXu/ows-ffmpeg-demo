package com.goodsogood.ows.helper

import com.goodsogood.ows.component.VideoProcessorProperties
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import ws.schild.jave.encode.VideoAttributes
import ws.schild.jave.encode.enums.X264_PROFILE
import ws.schild.jave.info.VideoSize
import java.io.File
import java.io.IOException
import java.util.*

/**
 * @author xuliduo
 * @date 2021/2/22
 * @description class VideoProcessor
 */
object VideoProcessor {
    private val log: Logger = LogManager.getLogger(VideoProcessor::class.java)

    /**
     * @param source 源视频文件
     * @param properties 配置
     * @return 压缩后的视频文件和缩略图
     */
    @Throws(IOException::class, Exception::class)
    fun compress(source: File, properties: VideoProcessorProperties): VideoOutput {
        val fileName = UUID.randomUUID().toString()
        // 因为目录包含了缩略图，因此实际文件路径例子如下:
        // 如文件名为"abc"，tempPath为"/var/tmp"，那么!!![目录]!!!为/var/tmp/abc
        val path = "${properties.tempPath}${File.separator}$fileName"
        FileUtils.forceMkdir(File(path))
        // 实际文件路径是 /var/tmp/abc/abc.suffix
        val target = File("$path${File.separator}${fileName}.${properties.ffmpegConfigs.format}")
        // audio
        val audio = AudioAttributes().setCodec(properties.ffmpegConfigs.audio.codec)
            .setBitRate(properties.ffmpegConfigs.audio.bitRate * 1000)
            .setChannels(properties.ffmpegConfigs.audio.channels)
            .setSamplingRate(properties.ffmpegConfigs.audio.samplingRate)
        // video
        val video = VideoAttributes().setCodec(properties.ffmpegConfigs.video.codec)
            .setX264Profile(X264_PROFILE.BASELINE)
            .setBitRate(properties.ffmpegConfigs.video.bitRate * 1000)
            .setFrameRate(properties.ffmpegConfigs.video.frameRate)
            .setSize(VideoSize.hd480)
            // .setSize(VideoSize.hd720)
        // 编码器
        val attrs = EncodingAttributes().setOutputFormat(properties.ffmpegConfigs.format)
            .setAudioAttributes(audio)
            .setVideoAttributes(video)
        // 转码
        val videoOutput = VideoOutput()
        try {
            val encoder = Encoder()
            encoder.encode(MultimediaObject(source), target, attrs)
            videoOutput.success = true
            videoOutput.target = target
            videoOutput.suffix = properties.ffmpegConfigs.format
        } catch (e: Exception) {
            videoOutput.success = false
            log.error("转码发生异常：${e.localizedMessage}", e)
        }
        return videoOutput
    }

    data class VideoOutput(
        var success: Boolean = true,
        var target: File?, // 文件名没有后缀。文件路径为 xxx/xxxx/filename.suffix
        var suffix: String?,
        val thumbnails: MutableList<File>
    ) {
        constructor() : this(true, null, null, mutableListOf())

        fun addThumbnail(file: File): VideoOutput {
            thumbnails.add(file)
            return this
        }
    }
}