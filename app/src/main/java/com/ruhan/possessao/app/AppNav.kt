package com.ruhan.possessao.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ruhan.possessao.ui.screens.*

@Composable
fun AppNav(vm: MainViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "start") {
        composable("start") { StartScreen(onContinue = { nav.navigate("sex") }) }
        composable("sex") { SexScreen(vm, onNext = { nav.navigate("age") }) }
        composable("age") { AgeScreen(vm, onNext = { nav.navigate("questions") }) }
        composable("questions") {
            QuestionsScreen(
                vm = vm,
                onPhoto = { nav.navigate("camera") },
                onGenerate = { vm.generateResult(); nav.navigate("result") },
                onManualSelect = { nav.navigate("entity_select") }
            )
        }
        composable("entity_select") { EntitySelectScreen(vm, onEntitySelected = { nav.navigate("result") }) }
        composable("camera") { CameraScreen(onPhotoCaptured = { uri, cameraType ->
            vm.setPhoto(uri, cameraType)
            nav.popBackStack()
        }) }
        composable("result") { ResultScreen(vm,
            onSeeTraditions = { nav.navigate("traditions") },
            onRestart = { vm.resetAll(); nav.navigate("start") }
        ) }
        composable("traditions") { TraditionsScreen(vm, onDone = { nav.navigate("final") }) }
        composable("final") { FinalScreen(onRestart = { vm.resetAll(); nav.navigate("start") }) }
    }
}

