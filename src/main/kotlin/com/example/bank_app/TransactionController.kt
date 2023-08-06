package com.example.bank_app

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import java.lang.NumberFormatException
import java.math.BigDecimal

class TransactionController {
    @FXML
    private lateinit var accountNumber: TextField
    @FXML
    private lateinit var amount: TextField
    @FXML
    private lateinit var status: Label
    @FXML
    private lateinit var submit: Button

    fun toBalanceScreen(e: ActionEvent) {
        SceneController.switchScene(5, e)
    }

    fun completeTransaction() {
        val initialBalance = BigDecimal(Data.balance)
        var deductedAmount: BigDecimal
        try {
            deductedAmount = BigDecimal(amount.text)
        } catch(e: NumberFormatException) {
            status.text = "Invalid amount"
            status.textFill = Color.RED
            return
        }

        if (accountNumber.text.length != 8) {
            status.text = "Invalid account number"
            status.textFill = Color.RED
            return
        }
        while(Data.accountNumber[0] == '0') {
            Data.accountNumber = Data.accountNumber.substring(1)
        }

        if(deductedAmount.scale() > 2 || deductedAmount < BigDecimal.ZERO) {
            status.text = "Invalid amount"
            status.textFill = Color.RED
            return
        }
        deductedAmount = deductedAmount.setScale(2)

        if(deductedAmount > initialBalance) {
            status.text = "Insufficient funds"
            status.textFill = Color.RED
            return
        }

        val newBalance = initialBalance.minus(deductedAmount)
        newBalance.setScale(2)
        Data.encryptAndSend("t,${Data.accountNumber},${accountNumber.text},$deductedAmount,$newBalance")
        val newMessage = Data.receiveAndDecrypt()
        if(newMessage == "f") {
            status.text = "Account does not exist"
            status.textFill = Color.RED
            return
        } else if(newMessage == "r") {
            status.text = "Processing error. Retry."
            status.textFill = Color.ORANGE
        }
        Data.balance = newBalance.toString()

        status.text = "Success!"
        status.textFill = Color.GREEN

        submit.isVisible = false
    }
}