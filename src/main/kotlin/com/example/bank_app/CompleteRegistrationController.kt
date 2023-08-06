package com.example.bank_app

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.Label

class CompleteRegistrationController {
    @FXML
    private lateinit var firstNameLabel: Label
    @FXML
    private lateinit var surnameLabel: Label
    @FXML
    private lateinit var dobLabel: Label
    @FXML
    private lateinit var sortCodeLabel: Label
    @FXML
    private lateinit var accountNumberLabel: Label

    fun fillDetails() {
        firstNameLabel.text = "First Name: ${Data.firstName}"
        surnameLabel.text = "Surname: ${Data.surname}"
        dobLabel.text = "Date of Birth: ${Data.dob}"
        sortCodeLabel.text = "Sort Code: ${Data.sortCode}"
        accountNumberLabel.text ="Account Number: ${Data.accountNumber}"
    }

    fun onButtonClick(e: ActionEvent) {
        SceneController.switchScene(5, e)
    }
}