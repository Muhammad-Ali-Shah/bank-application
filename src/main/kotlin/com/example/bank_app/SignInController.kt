package com.example.bank_app

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

class SignInController {
    @FXML
    private lateinit var firstNameField: TextField
    @FXML
    private lateinit var surnameField: TextField
    @FXML
    private lateinit var dobField: TextField
    @FXML
    private lateinit var sortCodeField: TextField
    @FXML
    private lateinit var accountNumberField: TextField
    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private fun onSubmitButtonClick(e: ActionEvent) {
        Data.firstName = firstNameField.text.replaceFirstChar { it.uppercase() }
        Data.surname = surnameField.text.replaceFirstChar { it.uppercase() }
        Data.dob = dobField.text
        Data.sortCode = sortCodeField.text
        Data.accountNumber = accountNumberField.text

        if(!Data.checkDate()) {
            errorLabel.text = "Invalid date format"
            return
        }

        val splitSortCode = Data.sortCode.split("-")
        for (num: String in splitSortCode) {
            if (num.length != 2) {
                errorLabel.text = "Invalid sort code format"
                return
            }
        }

        if (Data.accountNumber.length != 8) {
            errorLabel.text = "Invalid account number format (should be 8 digits)"
            return
        }
        while(Data.accountNumber[0] == '0') {
            Data.accountNumber = Data.accountNumber.substring(1)
        }

        val message = "si,${Data.firstName},${Data.surname},${Data.dob},${Data.sortCode},${Data.accountNumber}"
        Data.encryptAndSend(message)
        val receivedMessage = Data.receiveAndDecrypt()

        if(receivedMessage[1] == 'f') {
            errorLabel.text = "Incorrect details"
            return
        }

        Data.balance = receivedMessage.split(",")[1]
        SceneController.switchScene(5, e)
    }

    @FXML
    private fun onCancelButtonClick(e: ActionEvent) {
        SceneController.switchScene(2, e)
    }
}