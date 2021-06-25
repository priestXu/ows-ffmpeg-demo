package com.goodsogood.ows

import com.goodsogood.ows.component.VideoProcessorProperties
import com.goodsogood.ows.helper.VideoProcessor
import com.goodsogood.ows.helper.moderation.huawei.ModerationImageContent
import net.bramp.ffmpeg.FFmpegUtils
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import org.apache.commons.lang3.time.StopWatch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.event.annotation.BeforeTestClass
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


@SpringBootTest
class OwsFfmpegDemoApplicationTests {
    val log: Logger = LogManager.getLogger(OwsFfmpegDemoApplicationTests::class.java)

    @Autowired
    lateinit var videoProcessorProperties: VideoProcessorProperties

    val fileName: String = "output"

    // 跳舞
    val sourceFile = "/Users/xuliduo/Downloads/Phut_Hon_Phao_KAIZ_Remix_1080p.mp4"
    // 领导人
    // val sourceFile = "/Users/xuliduo/Downloads/中华人民共和国成立70周年_中共中央总书记国家主席中央军委主席习近平发表重要讲话_CCTV_1080p.mp4"

    // pron
    // val sourceFile = "/Users/xuliduo/Downloads/1080P_8000K_341610861.mp4"
    val titleFile = "/Users/xuliduo/workspaces/IdeaProjects/ows-ffmpeg-demo/tmp/output/title/tile.png"

    val waterMark = "/Users/xuliduo/Pictures/头像/炮姐.png"

    @Test
    fun contextLoads() {
    }

    @Test
    fun testAll() {
        val stopWatch = StopWatch()
        stopWatch.start()
        log.info("全部任务开始")
        this.testMakeTitle()
        this.testCompress()
        this.testThumbnail()
        this.testImageContentCheck()
        log.info("耗时${stopWatch}")
        stopWatch.stop()
    }

    @Test
    fun testCompress() {
        val stopWatch = StopWatch()
        stopWatch.start()
        log.info("开始转换")
        // Using the FFmpegProbeResult determine the duration of the input
        val output = VideoProcessor.compress(
            File(sourceFile),
            fileName,
            videoProcessorProperties,
            waterMark
        ) {
            println(
                String.format(
                    "status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
                    it.status,
                    it.frame,
                    FFmpegUtils.toTimecode(it.out_time_ns, TimeUnit.NANOSECONDS),
                    it.fps.toDouble(),
                    it.speed
                )
            )
        }
        log.info(output.target?.absolutePath)
        log.info("耗时${stopWatch}")
        stopWatch.stop()
    }

    @Test
    fun testMakeTitle() {
        val stopWatch = StopWatch()
        stopWatch.start()
        log.info("开始生成")
        val output = VideoProcessor.makeTitle(
            File(sourceFile),
            fileName,
            videoProcessorProperties
        ) {
            // do nothing
        }
        log.info(output.title)
        log.info("耗时${stopWatch}")
        stopWatch.stop()
    }

    @Test
    fun testThumbnail() {
        val stopWatch = StopWatch()
        stopWatch.start()
        log.info("开始生成")
        val output = VideoProcessor.thumbnail(
            File(sourceFile),
            fileName,
            videoProcessorProperties,
        ) {
            // do nothing
        }
        log.info(output.title)
        log.info("耗时${stopWatch}")
        stopWatch.stop()
    }

    @Test
    fun testImageContentCheck() {
        val stopWatch = StopWatch()
        stopWatch.start()
        log.info("验证")
        val imageCheckResult = ModerationImageContent(videoProcessorProperties).imageContentCheck(File(titleFile))
        log.info(imageCheckResult?.result?.suggestion)
        if (imageCheckResult?.result?.suggestion == "block") {
            log.info("ad:${imageCheckResult.result.category_suggestions.ad}")
            log.info("politics:${imageCheckResult.result.category_suggestions.politics}")
            log.info("porn:${imageCheckResult.result.category_suggestions.porn}")
            log.info("terrorism:${imageCheckResult.result.category_suggestions.terrorism}")
        }
        log.info("耗时${stopWatch}")
        stopWatch.stop()
    }

    @Test
    fun testFfprobe() {
        val ffprobe = FFprobe()
        val probeResult: FFmpegProbeResult = ffprobe.probe(sourceFile)
        val format = probeResult.getFormat()
        System.out.format(
            "%nFile: '%s' ; Format: '%s' ; Duration: %.3fs ; nb_streams: %d",
            format.filename,
            format.format_long_name,
            format.duration,
            format.nb_streams
        )

        val stream = probeResult.getStreams()[0]
        System.out.format(
            "%nCodec: '%s' ; Width: %dpx ; Height: %dpx ; nb_frames: %d",
            stream.codec_long_name,
            stream.width,
            stream.height,
            stream.nb_frames,
        )
    }

}
