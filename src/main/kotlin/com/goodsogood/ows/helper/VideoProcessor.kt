package com.goodsogood.ows.helper

import com.goodsogood.ows.component.VideoProcessorProperties
import com.goodsogood.ows.helper.bean.VideoSize
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.progress.ProgressListener
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
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
     * @param fileName 目标文件名称（不用添加后缀，后缀通过配置文件添加）
     * @param properties 配置
     * @param listener 任务进度监听实现
     * @return 压缩后的视频文件
     */
    @Throws(IOException::class, Exception::class)
    fun compress(
        source: File,
        fileName: String,
        properties: VideoProcessorProperties,
        listener: ProgressListener
    ): VideoOutput {
        if (!source.exists() || !source.isFile) {
            throw IOException("源文件不存在或者不是文件")
        }
        // 因为目录包含了缩略图，因此实际文件路径例子如下:
        // 如文件名为"abc"，tempPath为"/var/tmp"，那么!!![目录]!!!为/var/tmp/abc
        val path = "${properties.tempPath}${File.separator}$fileName"
        FileUtils.forceMkdir(File(path))
        // 实际文件路径是 /var/tmp/abc/abc.suffix
        val target = File("$path${File.separator}${fileName}.${properties.ffmpegConfigs.format}")

        val builder = FFmpegBuilder()
            .setInput(source.absolutePath)
            .overrideOutputFiles(true)
            .addOutput(target.absolutePath)
            .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
            // audio
            .setAudioChannels(properties.ffmpegConfigs.audio.channels)
            .setAudioCodec(properties.ffmpegConfigs.audio.codec)
            .setAudioSampleRate(properties.ffmpegConfigs.audio.samplingRate)
            .setAudioBitRate(properties.ffmpegConfigs.audio.bitRate * 1000L)
            // video
            .setFormat(properties.ffmpegConfigs.format)
            .setVideoCodec(properties.ffmpegConfigs.video.codec)
            .setVideoFrameRate(properties.ffmpegConfigs.video.frameRate, 1)
            .setVideoResolution(VideoSize.hd480.asEncoderArgument())
            // done
            .done()
        // 编码器
        val executor = FFmpegExecutor(FFmpeg(), FFprobe())
        // 转码
        val job = executor.createJob(builder, listener)
        job.run()
        return VideoOutput(target, properties.ffmpegConfigs.format)
    }

    /**
     * 产生 2XN 的title 图
     * @param source 源视频文件
     * @param fileName 目标文件名称（不用添加后缀，后缀通过配置文件添加）
     * @param properties 配置
     * @param listener 任务进度监听实现
     * @return title图
     */
    fun makeTitle(
        source: File,
        fileName: String,
        properties: VideoProcessorProperties,
        listener: ProgressListener
    ): VideoOutput {
        val probeResult: FFmpegProbeResult = FFprobe().probe(source.absolutePath)
        var duration = (probeResult.streams[0].duration / properties.sampleNum).toInt()
        duration = if (duration <= 0) {
            1
        } else {
            duration
        }
        // 目标路径
        val path = "${properties.tempPath}${File.separator}$fileName${File.separator}title"
        FileUtils.forceMkdir(File(path))
        val builder = FFmpegBuilder()
            .setInput(probeResult)
            .overrideOutputFiles(true)
            .addOutput("$path${File.separator}tile.png")
            // filter
            .setVideoFilter("select=(gte(t\\,${duration}))*(isnan(prev_selected_t)+gte(t-prev_selected_t\\,${duration})),scale=480:-2,tile=2x${properties.sampleNum / 2}")
            // frames
            .setFrames(1)
            .done()
        //
        val executor = FFmpegExecutor(FFmpeg(), FFprobe())
        val job = executor.createJob(builder, listener)
        job.run()
        return VideoOutput("$path${File.separator}tile.png")
    }

    /**
     * 生成缩略图
     */
    fun thumbnail(
        source: File,
        fileName: String,
        properties: VideoProcessorProperties,
        listener: ProgressListener
    ): VideoOutput {
        // 目标路径
        val path = "${properties.tempPath}${File.separator}$fileName${File.separator}thumbnails"
        FileUtils.forceMkdir(File(path))
        val probeResult: FFmpegProbeResult = FFprobe().probe(source.absolutePath)
        var duration = (probeResult.streams[0].duration / properties.thumbnailNum).toInt()
        duration = if (duration <= 0) {
            1
        } else {
            duration
        }
        val videoOutput = VideoOutput()
        val builder = FFmpegBuilder()
            .setInput(probeResult)
            .overrideOutputFiles(true)
            .addOutput("$path${File.separator}thumbnails%d.png")
            // -f
            .setFormat("image2")
            // -vf
            .setVideoFilter("crop=${properties.thumbnailSize},fps=1/${duration}")
            .done()
        //
        val executor = FFmpegExecutor(FFmpeg(), FFprobe())
        val job = executor.createJob(builder, listener)
        job.run()
        return videoOutput.also {
            it.addThumbnail(
                * Array(properties.thumbnailNum) { i ->
                    File("$path${File.separator}thumbnails${i}.png")
                }
            )
        }
    }


    data class VideoOutput(
        var success: Boolean = true,
        var target: File?, // 文件名没有后缀。文件路径为 xxx/xxxx/filename.suffix
        var suffix: String?,
        val thumbnails: MutableList<File>,
        val title: String?
    ) {
        constructor() : this(true, null, null, mutableListOf(), null)
        constructor(target: File?, suffix: String?) : this(true, target, suffix, mutableListOf(), null)
        constructor(title: String?) : this(true, null, null, mutableListOf(), title = title)

        fun addThumbnail(vararg files: File): VideoOutput {
            thumbnails.addAll(files)
            return this
        }
    }
}