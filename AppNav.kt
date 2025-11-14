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
        composable("ouija") { OuijaScreen(vm, onBack = { vm.resetAll(); nav.navigate("start") }) }
        composable("start") { StartScreen(onContinue = { nav.navigate("sex") }) }
        composable("sex") { SexScreen(vm, onNext = { nav.navigate("age") }) }
        composable("third_questions") { ThirdPersonQuestionsScreen(vm, onNext = { nav.navigate("age") }) }
        composable("age") { AgeScreen(vm, onNext = { nav.navigate("question_target") }) }
        composable("question_target") {
            QuestionTargetScreen(
                vm = vm,
                toFirstPersonQuestions = { nav.navigate("questions_first") },
                toThirdPersonQuestions = { nav.navigate("questions_third") }
            )
        }
        composable("questions_first") {
            QuestionsScreen(
                vm = vm,
                onPhoto = { nav.navigate("camera") },
                onGenerate = { vm.generateResult(); nav.navigate("result") },
                onManualSelect = { nav.navigate("entity_select") }
            )
        }
        composable("questions_third") {
            ThirdPersonQuestionsScreen(
                vm = vm,
                onNext = { nav.navigate("result") } // opcional: se quiser forçar foto antes, ajuste aqui
            )
            // Alternativa direta (sem pular pela 1ª pessoa):
            // ThirdPersonQuestionsScreen(vm, onNext = { vm.generateResult(); nav.navigate("result") })
        }
        composable("entity_select") { EntitySelectScreen(vm, onEntitySelected = { nav.navigate("result") }) }
        composable("camera") { CameraScreen(onPhotoCaptured = { uri, cameraType ->
            vm.setPhoto(uri, cameraType)
            nav.popBackStack()
        }) }
        composable("result") {
            ResultScreen(
                vm = vm,
                onSeeTraditions = { nav.navigate("traditions") },
                onRestart = { vm.resetAll(); nav.navigate("start") },
                onTalkToDemon = { nav.navigate("ouija") }
            )
        }
        composable("traditions") { TraditionsScreen(vm, onDone = { nav.navigate("final") }) }
        composable("final") {
            FinalScreen(
                onRestart = { vm.resetAll(); nav.navigate("start") },
                onTalkToDemon = { nav.navigate("ouija") }
            )
        }
    }
}

