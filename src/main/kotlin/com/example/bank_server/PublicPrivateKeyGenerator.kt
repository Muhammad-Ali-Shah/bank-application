package com.example.bank_server

import java.io.FileOutputStream
import java.security.KeyPairGenerator

fun main() {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(1024)
    val keyPair = keyGen.generateKeyPair()
    val publicKey = keyPair.public //format: X.509
    val privateKey = keyPair.private //format: PKCS#8



    var outputStream = FileOutputStream(Data.privateKeyLocation)
    outputStream.write(privateKey.encoded)
    outputStream.close()

    outputStream = FileOutputStream(com.example.bank_app.Data.publicKeyLocation)
    outputStream.write(publicKey.encoded)
    outputStream.close()
    println(publicKey.format)
    println(privateKey.format)
}