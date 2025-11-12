package com.ruhan.possessao.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.ruhan.possessao.data.db.AppDatabase
import com.ruhan.possessao.ui.theme.AppTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var vm: MainViewModel? = null
        var db: AppDatabase? = null
        var initError: String? = null

        try {
            db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "possessao.db"
            )
                .fallbackToDestructiveMigration()
                .build()

            // Criar o ViewModel via ViewModelProvider (não-composable) e passá-lo para o conteúdo composable
            vm = ViewModelProvider(this, MainViewModelFactory(application, db)).get(MainViewModel::class.java)
        } catch (e: Exception) {
            Log.e("MainActivity", "Falha na inicialização do DB/ViewModel", e)
            initError = e.message ?: "Erro na inicialização"
        }

        setContent {
            AppTheme {
                // Aplicar cor das barras do sistema conforme o tema (em contexto Composable)
                ApplySystemUiColors()

                if (initError != null) {
                    ErrorScreen(message = initError)
                } else {
                    AppNav(vm!!)
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(message: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Erro ao inicializar o aplicativo: ${message ?: "desconhecido"}")
    }
}

@Composable
private fun ApplySystemUiColors() {
    val view = LocalView.current
    val colors = MaterialTheme.colorScheme
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
}
