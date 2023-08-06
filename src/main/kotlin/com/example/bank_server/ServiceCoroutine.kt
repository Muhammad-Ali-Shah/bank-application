package com.example.bank_server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetProvider

class ServiceCoroutine(private val serviceSocket: Socket)  {
    private val rsaCipher = Cipher.getInstance("RSA")
    private var aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    private val privateKeyPath: Path = Paths.get(Data.privateKeyLocation)
    private val privateKeyBytes: ByteArray = Files.readAllBytes(privateKeyPath)

    private val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
    private val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
    private val privateKey: PrivateKey = keyFactory.generatePrivate(keySpec)

    private val stream = FileInputStream(Data.aesKeyLocation)
    private val aesKeyBytes: ByteArray = stream.readAllBytes()
    private val aesKey = SecretKeySpec(aesKeyBytes, "AES")

    private val conn = DriverManager.getConnection(Data.databaseUrl, Data.username, Data.password)
    private lateinit var rs: ResultSet
    private lateinit var psmt: PreparedStatement

    init {
        Class.forName("org.postgresql.Driver")
        stream.close()
    }

    fun start() = runBlocking {
        launch {
            println("SERVICE COROUTINE: Launched")
            val reader = withContext(Dispatchers.IO) {
                InputStreamReader(serviceSocket.getInputStream())
            }
            val sb = StringBuilder()
            var c = withContext(Dispatchers.IO) {
                reader.read()
            }.toChar()
            while (c != '#') {
                sb.append(c)
                c = withContext(Dispatchers.IO) {
                    reader.read()
                }.toChar()
            }
            val encryptedMessage = sb.toString()
            println("SERVICE COROUTINE: Data received")

            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            println("SERVICE COROUTINE: Private key received")

            val messageBytes: ByteArray = rsaCipher.doFinal(Base64.getDecoder().decode(encryptedMessage))
            val message = String(messageBytes)
            println("SERVICE COROUTINE: Message encrypted")
            //println(message)
//            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
//            println(Base64.getEncoder().encodeToString(aesCipher.doFinal("0.00".toByteArray())))

            when(message.split(",")[0]) {
                "ca" -> withContext(Dispatchers.IO) { createAccount(message) }
                "si" -> withContext(Dispatchers.IO) { signIn(message) }
                "t" -> transaction(message)
            }

        }
    }

    private fun createAccount(message: String) {
        val data: List<String> = message.split(",")
        var accountNumberFound = false
        val random = Random()
        var encryptedAccountNumber = ""
        var accountNumber = -1

        while(!accountNumberFound) {
            val checkedAccountNumber = random.nextInt(100000000).toString()
            val encryptedCheckedAccountNumber = encryptAccountNumber(checkedAccountNumber)
            val matchingAccountNumbers: CachedRowSet = getRecord(encryptedCheckedAccountNumber)

            if(!matchingAccountNumbers.next()) {
                accountNumberFound = true
                encryptedAccountNumber = encryptedCheckedAccountNumber
                accountNumber = checkedAccountNumber.toInt()
            }
        }
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        val sortCode = "07-07-04"
        val encryptedSortCode = Base64.getEncoder().encodeToString(aesCipher.doFinal(sortCode.toByteArray()))
        val encryptedFirstName = Base64.getEncoder().encodeToString(aesCipher.doFinal(data[1].toByteArray()))
        val encryptedSurname = Base64.getEncoder().encodeToString(aesCipher.doFinal(data[2].toByteArray()))
        val encryptedDob = Base64.getEncoder().encodeToString(aesCipher.doFinal(data[3].toByteArray()))
        println("SERVICE COROUTINE: Details generated")

        psmt = conn.prepareStatement("INSERT INTO records VALUES (?, ?, ?, ?, ?, ?)")
        psmt.setString(1, encryptedSortCode)
        psmt.setString(2, encryptedAccountNumber)
        psmt.setString(3, encryptedFirstName)
        psmt.setString(4, encryptedSurname)
        psmt.setString(5, encryptedDob)
        psmt.setString(6, Base64.getEncoder().encodeToString(aesCipher.doFinal("0.00".toByteArray())))
        psmt.executeUpdate()
        println("SERVICE COROUTINE: Records added to database")
        val newMessage = "$message,$sortCode,$accountNumber"
        encryptAndSend(newMessage)
    }

    private fun signIn(message: String) {
        val splitMessage = message.split(",")
        val accountNumber = splitMessage[5]
        val encryptedAccountNumber = encryptAccountNumber(accountNumber)
        val matchingAccount = getRecord(encryptedAccountNumber)
        if(!matchingAccount.next()) {
            encryptAndSend("f")
            return
        }
        println("SERVICE COROUTINE: Database queried")

        aesCipher.init(Cipher.DECRYPT_MODE, aesKey)
        val checkedSortCode = splitMessage[4]
        val checkedFirstName = splitMessage[1]
        val checkedSurname = splitMessage[2]
        val checkedDob = splitMessage[3]

        val actualSortCode =
            String(aesCipher.doFinal(Base64.getDecoder().decode(matchingAccount.getString("sort_code"))))
        val actualFirstName =
            String(aesCipher.doFinal(Base64.getDecoder().decode(matchingAccount.getString("first_name"))))
        val actualSurname =
            String(aesCipher.doFinal(Base64.getDecoder().decode(matchingAccount.getString("surname"))))
        val actualDob =
            String(aesCipher.doFinal(Base64.getDecoder().decode(matchingAccount.getString("dob"))))
        val balance =
            String(aesCipher.doFinal(Base64.getDecoder().decode(matchingAccount.getString("balance"))))

        val newMessage: String = if(
            checkedSortCode == actualSortCode &&
            checkedFirstName == actualFirstName &&
            checkedSurname == actualSurname &&
            checkedDob == actualDob
        ) {
            "s,$balance"
        } else {
            "f"
        }
        encryptAndSend(newMessage)
    }

    private fun getRecord(encryptedAccountNumber: String): CachedRowSet {
        psmt = conn.prepareStatement("SELECT * FROM records WHERE account_number = ?;",
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)

        psmt.setString(1, encryptedAccountNumber)
        rs = psmt.executeQuery()

        val aFactory = RowSetProvider.newFactory()
        val matchingAccountNumbers = aFactory.createCachedRowSet()
        matchingAccountNumbers.populate(rs)
        rs.beforeFirst()
        psmt.close()
        rs.close()

        return matchingAccountNumbers
    }

    private fun encryptAccountNumber(accountNumber: String): String {
        aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        val encryptedAccountNumberBytes = aesCipher.doFinal(accountNumber.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedAccountNumberBytes)
    }

    private fun encryptAndSend(newMessage: String) {
        rsaCipher.init(Cipher.ENCRYPT_MODE, privateKey)
        val encryptedMessageBytes: ByteArray = rsaCipher.doFinal(newMessage.toByteArray(StandardCharsets.UTF_8))
        val encryptedMessage = String(Base64.getEncoder().encode(encryptedMessageBytes))
        println("SERVICE COROUTINE: Message encrypted")

        val writer = OutputStreamWriter(serviceSocket.getOutputStream())
        writer.write("$encryptedMessage#")
        writer.flush()
        println("SERVICE COROUTINE: Message sent to client")
        println("END OF SERVICE COROUTINE")
    }

    private fun transaction(message: String) {
        val splitMessage = message.split(",")
        val sendAccountNumber = splitMessage[1]
        val receiveAccountNumber = splitMessage[2]
        val deductedAmount = BigDecimal(splitMessage[3])
        val newSendBalance = splitMessage[4]

        val encryptedReceiveAccountNumber = encryptAccountNumber(receiveAccountNumber)
        val receiveRecord = getRecord(encryptedReceiveAccountNumber)
        println("SERVICE COROUTINE: Receiver records retrieved")

        if(!receiveRecord.next()) {
            encryptAndSend("f")//account number entered does not exist
        }

        val encryptedSendAccountNumber = encryptAccountNumber(sendAccountNumber)
        updateBalance(newSendBalance, encryptedSendAccountNumber)
        println("SERVICE COROUTINE: Receiver records updated")

        aesCipher.init(Cipher.DECRYPT_MODE, aesKey)
        var receiveBalance = BigDecimal(
            String(aesCipher.doFinal(Base64.getDecoder().decode(receiveRecord.getString("balance")))))
        receiveBalance = deductedAmount.plus(receiveBalance)
        updateBalance(receiveBalance.toString(), encryptedReceiveAccountNumber)

        encryptAndSend("s")//success!
    }

    private fun updateBalance(balance: String, encryptedAccountNumber: String) {
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        psmt = conn.prepareStatement("UPDATE records SET balance = ? WHERE account_number = ?")
        psmt.setString(
            1,
            Base64.getEncoder().encodeToString(aesCipher.doFinal(balance.toByteArray()))
        )
        psmt.setString(2, encryptedAccountNumber)
        try {
            psmt.executeUpdate()
        } catch(e: SQLException) {
            encryptAndSend("r")//retry
        }

        psmt.close()
    }
}