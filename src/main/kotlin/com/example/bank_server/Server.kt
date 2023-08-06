package com.example.bank_server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.ServerSocket

fun main() = runBlocking {
    var serverSocket = withContext(Dispatchers.IO) {
        ServerSocket(
            Data.port, Data.serverBacklog,
            InetAddress.getByName(Data.serverUrl)
        )
    }
    while(true) {
        println("SERVER: Awaiting request")
        val request = withContext(Dispatchers.IO) {
            serverSocket.accept()
        }
        println("SERVER: Request received. Initialising request handler coroutine.")
        launch {
            val requestHandler = ServiceCoroutine(request)
            requestHandler.start()
        }
        println("SERVER: Finished loop")
    }
}