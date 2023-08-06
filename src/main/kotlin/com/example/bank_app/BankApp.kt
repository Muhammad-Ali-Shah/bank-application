package com.example.bank_app

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class BankApp : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(BankApp::class.java.getResource("welcome-screen.fxml"))
        val scene = Scene(fxmlLoader.load(), 640.0,480.0)
        stage.title = "MAS Bank"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(BankApp::class.java)
}