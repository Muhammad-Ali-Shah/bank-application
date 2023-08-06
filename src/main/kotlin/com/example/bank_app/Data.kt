package com.example.bank_app

import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher

object Data {
    //server data
    private const val server_host = "127.0.0.1"
    private const val server_port = 8080
    const val publicKeyLocation = "src/main/resources/com/example/bank_app/public-key.pub"

    //user details
    var sortCode = ""
    var accountNumber = ""
    var firstName = ""
    var surname = ""
    var dob = ""
    var balance = "0.00"

    //server and encryption/decryption
    private val publicKeyPath: Path = Paths.get(publicKeyLocation)
    private val publicKeyBytes: ByteArray = Files.readAllBytes(publicKeyPath)
    private val keySpec = X509EncodedKeySpec(publicKeyBytes)
    private val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
    private val publicKey: PublicKey = keyFactory.generatePublic(keySpec)
    private val cipher: Cipher = Cipher.getInstance("RSA")
    private lateinit var clientSocket: Socket

    fun checkDate(): Boolean {
        val cal = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        df.isLenient = false
        try {
            cal.time = df.parse(dob)
            if (cal.get(Calendar.YEAR) < 1900) { return false }
            return true
        } catch(e: ParseException) {
            return false
        }
    }

    fun encryptAndSend(message: String) {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedMessageBytes: ByteArray = cipher.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        val encryptedMessage = String(Base64.getEncoder().encode(encryptedMessageBytes))

        clientSocket = Socket(InetAddress.getByName(server_host), server_port)
        val writer = OutputStreamWriter(clientSocket.getOutputStream())
        writer.write("$encryptedMessage#")
        writer.flush()
    }

    fun receiveAndDecrypt(): String {
        val reader = InputStreamReader(clientSocket.getInputStream())
        val sb = StringBuilder()
        var c = reader.read().toChar()
        while (c != '#') {
            sb.append(c)
            c = reader.read().toChar()
        }
        val encryptedMessage = sb.toString()
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        val messageBytes: ByteArray = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage))
        clientSocket.close()
        return String(messageBytes)
    }

}