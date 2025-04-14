package com.diploma.work.data.grpc

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
            channel = ManagedChannelBuilder.forTarget("10.0.2.2:50051")
                .usePlaintext()
                .build()
        }
        return channel!!
    }

    fun shutdown() {
        channel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
    }
}