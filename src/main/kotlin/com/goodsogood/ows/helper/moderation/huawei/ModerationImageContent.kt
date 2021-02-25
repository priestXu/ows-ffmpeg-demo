package com.goodsogood.ows.helper.moderation.huawei

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.goodsogood.ows.component.VideoProcessorProperties
import com.goodsogood.ows.helper.ResponseProcessUtils
import com.goodsogood.ows.helper.bean.ImageCheckResult
import com.huawei.ais.sdk.AisAccess
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.http.entity.StringEntity
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException

/**
 * @author xuliduo
 * @date 2021/2/25
 * @description class 图像检查类
 */
class ModerationImageContent(videoProcessorProperties: VideoProcessorProperties) {
    private val log: Logger = LogManager.getLogger(ModerationImageContent::class.java)
    private var service: AisAccess
    private var properties: VideoProcessorProperties = videoProcessorProperties
    private val imageUri = "/v1.0/moderation/image"
    private var mapper = ObjectMapper().registerKotlinModule().apply {
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // Ignore null values when writing json.
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        // Write times as a String instead of a Long so its human readable.
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        this.registerModule(JavaTimeModule())
    }

    /**
     * 为枚举开放一个查询方法
     */
    private inline fun <reified T : Enum<T>, V> ((T) -> V).find(value: V): T? {
        return enumValues<T>().firstOrNull { this(it) == value }
    }

    init {
        /**
         * 初始化华为服务器验证
         */
        service = ServiceAccessBuilder.builder()
            .ak(videoProcessorProperties.huawei.ak)
            .sk(videoProcessorProperties.huawei.sk)
            .region(ServiceAccessBuilder.ServiceName::value.find(videoProcessorProperties.huawei.region))
            .build()
    }

    @Throws(IOException::class, NullPointerException::class)
    fun imageContentCheck(image: File): ImageCheckResult? {
        if (!image.exists() || !image.isFile) {
            throw IOException("输入的图片文件不存在")
        }
        val base64Image = Base64.encodeBase64String(FileUtils.readFileToByteArray(image))
        // api请求参数说明可参考: https://support.huaweicloud.com/api-moderation/moderation_03_0019.html
        val data = mapOf(
            // 审核图片
            "image" to base64Image,
            // 审核内容
            "categories" to properties.huawei.categories,
            // 可信区间 0~1，不穿为默认
            // "threshold" to 0
        )
        // post
        val stringEntity = StringEntity(ObjectMapper().writeValueAsString(data), Charsets.UTF_8)
        val response = service.post(imageUri, stringEntity)
        // 验证服务调用返回的状态是否成功，如果为200, 为成功, 否则失败。
        if (!ResponseProcessUtils.isRespondedOK(response)) {
            log.error("请求失败...")
            return null
        }
        // 获得结果
        return mapper.readValue(ResponseProcessUtils.processResponse(response), ImageCheckResult::class.java)
    }
}