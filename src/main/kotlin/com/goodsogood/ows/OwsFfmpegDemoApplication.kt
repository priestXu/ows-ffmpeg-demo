package com.goodsogood.ows

import com.goodsogood.ows.component.VideoProcessorProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
class OwsFfmpegDemoApplication

fun main(args: Array<String>) {
    runApplication<OwsFfmpegDemoApplication>(*args)
}
