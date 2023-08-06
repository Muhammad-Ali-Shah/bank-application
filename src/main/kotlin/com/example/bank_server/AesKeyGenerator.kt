package com.example.bank_server

import java.io.FileOutputStream
import javax.crypto.KeyGenerator

fun main() {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(192)
    val key = keyGen.generateKey()

    val stream = FileOutputStream(Data.aesKeyLocation)
    stream.write(key.encoded)
    stream.close()
}