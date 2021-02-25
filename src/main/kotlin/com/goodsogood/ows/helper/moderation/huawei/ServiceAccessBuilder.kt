package com.goodsogood.ows.helper.moderation.huawei

import com.huawei.ais.common.AuthInfo
import com.huawei.ais.common.ProxyHostInfo
import com.huawei.ais.sdk.AisAccess
import com.huawei.ais.sdk.AisAccessWithProxy

/**
 * @author xuliduo
 * @date 2021/2/25
 * @description class ServiceAccessBuilder
 */
class ServiceAccessBuilder {
    enum class ServiceName(var value: String) {
        CN_NORTH_1("cn-north-1"),
        CN_NORTH_4("cn-north-4"),
        AP_SOUTHEAST_1("ap-southeast-1"),
        CN_EAST_3("cn-east-3"),
        AP_SOUTHEAST_3("ap-southeast-3");
    }

    object Endpoint {
        /**
         * 内容审核服务的区域和终端节点信息可以从如下地址查询
         * http://developer.huaweicloud.com/dev/endpoint
         */
        val endpoints = mapOf(
            ServiceName.CN_NORTH_1 to "https://moderation.cn-north-1.myhuaweicloud.com",
            ServiceName.CN_NORTH_4 to "https://moderation.cn-north-4.myhuaweicloud.com",
            ServiceName.AP_SOUTHEAST_1 to "https://moderation.ap-southeast-1.myhuaweicloud.com",
            ServiceName.AP_SOUTHEAST_3 to "https://moderation.ap-southeast-3.myhuaweicloud.com",
            ServiceName.CN_EAST_3 to "https://moderation.cn-east-3.myhuaweicloud.com",
        )
    }

    private var region: ServiceName? = null

    private var endpoint: String? = null

    private var ak: String? = null

    private var sk: String? = null

    private var proxy: ProxyHostInfo? = null

    private var connectionTimeout = 5000

    private var connectionRequestTimeout = 1000

    private var socketTimeout = 5000

    private var retryTimes = 3

    companion object {
        fun builder(): ServiceAccessBuilder {
            return ServiceAccessBuilder()
        }
    }

    fun build(): AisAccess {
        return if (proxy == null) {
            AisAccess(
                AuthInfo(endpoint, region?.value, ak, sk),
                connectionTimeout,
                connectionRequestTimeout,
                socketTimeout,
                retryTimes
            )
        } else {
            AisAccessWithProxy(
                AuthInfo(endpoint, region?.value, ak, sk),
                proxy,
                connectionTimeout,
                connectionRequestTimeout,
                socketTimeout,
                retryTimes
            )
        }
    }

    fun ak(ak: String): ServiceAccessBuilder {
        this.ak = ak
        return this
    }

    fun sk(sk: String): ServiceAccessBuilder {
        this.sk = sk
        return this
    }

    fun region(region: ServiceName?): ServiceAccessBuilder {
        this.region = region
        endpoint = getCurrentEndpoint(region)
        return this
    }

    fun proxy(proxy: ProxyHostInfo): ServiceAccessBuilder {
        this.proxy = proxy
        return this
    }

    fun connectionTimeout(connectionTimeout: Int): ServiceAccessBuilder {
        this.connectionTimeout = connectionTimeout
        return this
    }

    fun connectionRequestTimeout(connectionRequestTimeout: Int): ServiceAccessBuilder {
        this.connectionRequestTimeout = connectionRequestTimeout
        return this
    }

    fun socketTimeout(socketTimeout: Int): ServiceAccessBuilder {
        this.socketTimeout = socketTimeout
        return this
    }

    fun retryTimes(retryTimes: Int): ServiceAccessBuilder {
        this.retryTimes = retryTimes
        return this
    }

    /**
     * 用于支持使用代理模式访问网络， 此时使用的代理主机配置信息
     */
    fun getProxyHost(): ProxyHostInfo {
        return ProxyHostInfo(
            "proxycn2.***.com",  /* 代理主机信息 */
            8080,  /* 代理主机的端口 */
            "china/***",  /* 代理的用户名 */
            "***" /* 代理用户对应的密码 */
        )
    }

    /**
     * 用于根据服务的区域信息获取服务域名
     */
    fun getCurrentEndpoint(region: ServiceName?): String? {
        return ServiceAccessBuilder.Endpoint.endpoints[region]
    }

}