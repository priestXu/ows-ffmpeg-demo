package com.goodsogood.ows.component

import org.hibernate.validator.constraints.Range
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

object Prefixes {
    const val videoProcessorProperties: String = "vpc"
}

@Component
@ConfigurationProperties(prefix = Prefixes.videoProcessorProperties)
@Validated
class VideoProcessorProperties {
    @Valid
    var ffmpegConfigs: FFmpegConfigs = FFmpegConfigs()

    @Valid
    var huawei: Huawei = Huawei()

    // 抽样验证图片，最大10个
    // 系统会生成 2XN的title图，然后使用title图去进行内容审核
    @Range(min = 1, max = 10)
    var sampleNum: Int = 8

    // 缩略图，最多5个
    // 系统会采样包含第0秒开始均分的n秒缩略图
    @Range(min = 1, max = 5)
    var thumbnailNum: Int = 3

    // 缩略图的copy size
    var thumbnailHeight: Int = 450
    var thumbnailWidth: Int = 450
    var thumbnailFillColor: String = "black"

    // 转码后的临时文件路径
    @NotEmpty
    var tempPath: String = ""

}

@Validated
class FFmpegConfigs {
    // 转码后的视频格式 默认mp4
    @NotNull
    var format: String = "mp4"

    // 音频
    @Valid
    @NotNull
    var audio: Audio = Audio()

    // 视频
    @Valid
    @NotNull
    var video: Video = Video()
}

/**
 * 音频配置
 */
@Validated
class Audio {
    // 音频编码
    @NotEmpty
    var codec: String = "aac"

    // 音频比特率 默认64kbit/s
    @NotNull
    var bitRate: Int = 64

    // 声道
    @NotNull
    var channels: Int = 2

    // 采样率
    @NotNull
    var samplingRate: Int = 44100
}

/**
 * 视频配置
 */
@Validated
class Video {
    // 视频编码 - 目前只支持h264
    var codec: String = "h264"

    // 视频比特率 默认160 kbps
    @NotNull
    var bitRate: Int = 160

    // 帧显示频率
    // 帧数越多，质量和尺寸就越多，但基于移动设备等设备，帧数没必要那么大
    @NotNull
    var frameRate: Int = 15
    // 分辨率，只能是 480p，默认是480p
    //  val size: String = "480p"
}

/**
 * 华为内容审核配置
 */
@Validated
class Huawei {
    // ak
    var ak: String = ""

    // sk
    var sk: String = ""

    // region
    var region = "cn-north-4"

    // 验证类型
    var categories = mutableListOf("politics", "terrorism", "porn", "ad")
}