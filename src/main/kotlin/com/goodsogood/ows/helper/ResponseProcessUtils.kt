package com.goodsogood.ows.helper

import com.cloud.sdk.util.StringUtils
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.huawei.ais.sdk.util.HttpClientUtils
import org.apache.commons.codec.binary.Base64
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * 访问服务返回结果信息验证的工具类
 */
object ResponseProcessUtils {
    private val logger = LoggerFactory.getLogger(ResponseProcessUtils::class.java)

    /**
     * 打印出服务访问完成的HTTP状态码
     *
     * @param response 响应对象
     */
    fun processResponseStatus(response: HttpResponse) {
        println(response.statusLine.statusCode)
    }

    /**
     * 打印响应的状态码，并检测是否为200
     *
     * @param response
     * 响应对象
     * @return 如果状态码为200则返回true，否则返回false
     */
    fun isRespondedOK(response: HttpResponse): Boolean {
        val statusCode = response.statusLine.statusCode
        println(statusCode)
        return HttpStatus.SC_OK == statusCode
    }

    /**
     * 打印出服务访问完成后，转化为文本的字符流，主要用于JSON数据的展示
     *
     * @param response 响应对象
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    @Throws(UnsupportedOperationException::class, IOException::class)
    fun processResponse(response: HttpResponse): String {
        val str = HttpClientUtils.convertStreamToString(response.entity.content)
        println(str)
        return str
    }

    /**
     * 处理返回Base64编码的图像文件的生成
     *
     * @param result
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    @Throws(UnsupportedOperationException::class, IOException::class)
    fun processResponseWithImage(result: String?, fileName: String) {
        val resp = ObjectMapper().readValue(
            result,
            object : TypeReference<Map<String, Any>>() {}
        )
        val responseRlt = resp["result"] as Map<*, *>
        val imageString = responseRlt["data"] as String
        if (StringUtils.isNullOrEmpty(imageString)) {
            logger.info("The result without file string of base64, response {} ", resp)
        } else {
            val fileBytes = Base64.decodeBase64(imageString)
            writeBytesToFile(fileName, fileBytes)
        }
    }

    /**
     * 将字节数组写入到文件, 用于支持二进制文件(如图片)的生成
     * @param fileName 文件名
     * @param data 数据
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeBytesToFile(fileName: String, data: ByteArray?) {
        var fc: FileChannel? = null
        try {
            val bb = ByteBuffer.wrap(data)
            fc = FileOutputStream(fileName).channel
            fc.write(bb)
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("Failed to generate file is faild, cause {}", e)
        } finally {
            fc?.close()
        }
    }
}