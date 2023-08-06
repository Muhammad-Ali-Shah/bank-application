package com.example.bank_app

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label

class WelcomeController {
    @FXML
    private fun onCreateBankAccountButtonClick(e: ActionEvent) {
        SceneController.switchScene(1, e)
    }

    @FXML
    private fun onSignInButtonClick(e: ActionEvent) {
        SceneController.switchScene(4, e)
    }
}