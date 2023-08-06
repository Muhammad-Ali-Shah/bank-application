package com.example.bank_server

object Data {
    const val username = "postgres"
    const val password = "password"
    const val databaseUrl = "jdbc:postgresql://localhost:5432/bank"

    const val privateKeyLocation = "src/main/resources/com/example/bank_server/private-key.key"
    const val aesKeyLocation = "src/main/resources/com/example/bank_server/aes-key.enc"

    const val serverUrl = "127.0.0.1"
    const val port = 8080
    const val serverBacklog = 2
}

