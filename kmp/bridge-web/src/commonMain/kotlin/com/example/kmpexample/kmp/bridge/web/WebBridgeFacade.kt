package com.example.kmpexample.kmp.bridge.web

import com.example.kmpexample.kmp.core.bridge.web.WebBridgeFactory
import com.example.kmpexample.kmp.core.bridge.web.WebRootBridge

object WebBridgeFacade {
    fun create(baseUrl: String = "/api/rest"): WebRootBridge = WebBridgeFactory.create(baseUrl)
}

typealias GeneratedWebRoute = BridgeRoute

object GeneratedRouteTable {
    val routes: List<GeneratedWebRoute> = BridgeSchema.routes
}