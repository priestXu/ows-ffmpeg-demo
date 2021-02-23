package com.goodsogood.ows

import com.goodsogood.ows.component.VideoProcessorProperties
import com.goodsogood.ows.helper.VideoProcessor
import org.apache.commons.lang3.time.StopWatch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class OwsFfmpegDemoApplicationTests {
    val log: Logger = LogManager.getLogger(OwsFfmpegDemoApplicationTests::class.java)

    @Autowired
    lateinit var videoProcessorProperties: VideoProcessorProperties

    @Test
    fun contextLoads() {
    }

    @Test
    fun testCompress() {
        val stopWatch = StopWatch()
        stopWatch.start()
        log.info("开始转换")
        VideoProcessor.compress(File("/Users/xuliduo/Downloads/20210205_184816.mp4"), videoProcessorProperties)
        log.info("耗时${stopWatch}")
        stopWatch.stop()
    }

}
