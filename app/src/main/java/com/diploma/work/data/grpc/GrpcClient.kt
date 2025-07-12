package com.diploma.work.data.grpc

import com.diploma.work.utils.Constants
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrpcClient @Inject constructor() {
    private var channel: ManagedChannel? = null

    fun getChannel(): ManagedChannel {
        if (channel == null || channel!!.isShutdown) {
            channel = ManagedChannelBuilder.forTarget("${Constants.Network.AUTH_SERVER_HOST}:${Constants.Network.AUTH_SERVER_PORT}")
                .usePlaintext()
                .build()
        }
        return channel!!
    }

    fun shutdown() {
        channel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
    }
}