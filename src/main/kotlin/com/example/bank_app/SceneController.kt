package com.example.bank_app

import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class SceneController {
    companion object {

        fun switchScene(sceneNumber: Int, event: ActionEvent) {
            var screenName = "welcome-screen.fxml"
            when(sceneNumber) {
                1 -> screenName = "create-account-screen.fxml"
                2 -> screenName = "welcome-screen.fxml"
                3 -> screenName = "complete-registration-screen.fxml"
                4 -> screenName = "sign-in-screen.fxml"
                5 -> screenName = "balance-screen.fxml"
                6 -> screenName = "transaction-screen.fxml"
            }
            val root: Parent = FXMLLoader.load(BankApp::class.java.getResource(screenName))
            val stage: Stage = (event.source as Node).scene.window as Stage
            val scene = Scene(root, 640.0, 480.0)
            stage.scene = scene
            stage.show()
        }
    }
}