package com.example.bank_app

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Label

class BalanceController {
    @FXML
    private lateinit var balance: Label

    fun setBalance() {
        balance.text = Data.balance
    }

    fun toTransactionScreen(e: ActionEvent) {
        SceneController.switchScene(6, e)
    }
}