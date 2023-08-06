package com.example.bank_app

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField

class CreateAccountController {
    @FXML
    private lateinit var firstNameField: TextField
    @FXML
    private lateinit var surnameField: TextField
    @FXML
    private lateinit var dobField: TextField
    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private fun onCancelButtonClick(e: ActionEvent) {
        SceneController.switchScene(2, e)
    }

    @FXML
    private fun onSubmitButtonClick(e: ActionEvent) {
        Data.firstName = firstNameField.text.replaceFirstChar { it.uppercase() }
        Data.surname = surnameField.text.replaceFirstChar { it.uppercase() }
        Data.dob = dobField.text

        if(!Data.checkDate()) {
            errorLabel.text = "Invalid date format"
            return
        }

        var message = "ca,${Data.firstName},${Data.surname},${Data.dob}"

        Data.encryptAndSend(message)
        message = Data.receiveAndDecrypt()

        val splitMessage = message.split(",")
        Data.sortCode = splitMessage[4]
        Data.accountNumber = splitMessage[5]

        while(Data.accountNumber.length < 8) {
            Data.accountNumber = "0${Data.accountNumber}"
        }

        SceneController.switchScene(3, e)
    }
}