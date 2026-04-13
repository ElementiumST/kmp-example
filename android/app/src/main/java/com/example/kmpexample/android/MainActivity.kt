package com.example.kmpexample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.core.model.AuthScreenAction

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootComponent = SharedApp.createRootComponent()

        setContent {
            AndroidApp(rootComponent = rootComponent)
        }
    }
}

@Composable
private fun AndroidApp(rootComponent: RootComponent) {
    AndroidAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AuthRoute(rootComponent = rootComponent)
        }
    }
}

@Composable
private fun AuthRoute(rootComponent: RootComponent) {
    val state by rootComponent.authComponent.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }

        OutlinedTextField(
            value = state.login,
            onValueChange = {
                rootComponent.authComponent.onAction(AuthScreenAction.UpdateLogin(it))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Логин") },
            singleLine = true,
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = {
                rootComponent.authComponent.onAction(AuthScreenAction.UpdatePassword(it))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )

        Button(
            onClick = { rootComponent.authComponent.onAction(AuthScreenAction.Submit) },
            enabled = state.canSubmit,
        ) {
            Text(text = state.submitLabel)
        }

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (state.isAuthorized) {
            Text(
                text = "Вход выполнен: ${state.authorizedName ?: state.authorizedLogin.orEmpty()}",
                style = MaterialTheme.typography.titleMedium,
            )

            state.sessionId?.let { sessionId ->
                Text(
                    text = "Session: $sessionId",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
