package com.example.kmpexample.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent
import com.example.kmpexample.kmp.feature.auth.model.AuthScreenAction

@Composable
fun AuthRoute(component: AuthComponent) {
    val state by component.state.collectAsState()

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
            onValueChange = { component.onAction(AuthScreenAction.UpdateLogin(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Логин") },
            singleLine = true,
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = { component.onAction(AuthScreenAction.UpdatePassword(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )

        Button(
            onClick = { component.onAction(AuthScreenAction.Submit) },
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
    }
}
