package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.ComponentContext
import com.example.kmpexample.kmp.core.model.AuthScreenAction
import com.example.kmpexample.kmp.core.model.AuthScreenState
import com.example.kmpexample.kmp.domain.model.LoginRequest
import com.example.kmpexample.kmp.domain.usecase.LoginUseCase
import com.example.kmpexample.kmp.feature.base.BaseMviComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val loginUseCase: LoginUseCase,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BaseMviComponent<AuthScreenState, AuthScreenAction>(AuthScreenState()),
    AuthComponent,
    ComponentContext by componentContext {

    override fun onAction(action: AuthScreenAction) {
        when (action) {
            is AuthScreenAction.UpdateLogin -> updateState(login = action.value)
            is AuthScreenAction.UpdatePassword -> updateState(password = action.value)
            AuthScreenAction.Submit -> submit()
        }
    }

    private fun updateState(
        login: String = mutableState.value.login,
        password: String = mutableState.value.password,
    ) {
        val current = mutableState.value
        mutableState.value = current.copy(
            login = login,
            password = password,
            errorMessage = null,
            canSubmit = login.isNotBlank() && password.isNotBlank() && !current.isLoading,
        )
    }

    private fun submit() {
        val currentState = mutableState.value
        if (currentState.isLoading || !currentState.canSubmit) {
            return
        }

        coroutineScope.launch {
            mutableState.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
                canSubmit = false,
            )

            runCatching {
                loginUseCase(
                    LoginRequest(
                        login = currentState.login,
                        password = currentState.password,
                        rememberMe = false,
                    ),
                )
            }.onSuccess { session ->
                mutableState.value = mutableState.value.copy(
                    password = "",
                    isLoading = false,
                    isAuthorized = true,
                    sessionId = session.sessionId,
                    authorizedLogin = session.user.login,
                    authorizedName = session.user.name ?: session.user.login,
                    errorMessage = null,
                    canSubmit = false,
                )
            }.onFailure { throwable ->
                mutableState.value = mutableState.value.copy(
                    isLoading = false,
                    isAuthorized = false,
                    errorMessage = throwable.message ?: "Не удалось выполнить вход",
                    canSubmit = mutableState.value.login.isNotBlank() &&
                        mutableState.value.password.isNotBlank(),
                )
            }
        }
    }
}
