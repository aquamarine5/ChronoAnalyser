package org.aquamarine5.brainspark.chronoanalyser

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase

class ChronoMainCompose(private val context: Context) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DrawMainContent() {
        val isInstalled by remember{
            mutableStateOf(ChronoDatabase.getInstance(context).chronoConfigDAO().getConfig()!!.isInstalled)
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chrono Analyser") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            when(isInstalled){
                false->{
                    WelcomePage(innerPadding)
                }
                true->{

                }
            }
        }
    }

    @Composable
    fun WelcomePage(innerPaddingValues: PaddingValues){
        val isInstalling by remember { mutableStateOf(false) }
        when(isInstalling){
            false->{
                Button(onClick={
                    isInstalling=false
                }){

                }
            }
            true->{

            }
        }
        Button() { }
        val scope= rememberCoroutineScope()
        scope.launch {

        }
    }
}

