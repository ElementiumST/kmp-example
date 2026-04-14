package com.example.kmpexample.kmp.feature.auth.component

import com.arkivanov.decompose.ComponentContext
import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest
import com.example.kmpexample.kmp.domain.usecase.ClearPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.GetPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginWithTokenUseCase
import com.example.kmpexample.kmp.feature.auth.model.AuthScreenAction
import com.example.kmpexample.kmp.feature.auth.model.AuthScreenState
import com.example.kmpexample.kmp.feature.base.BaseMviComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val clearPersistedAuthSessionUseCase: ClearPersistedAuthSessionUseCase,
    private val getPersistedAuthSessionUseCase: GetPersistedAuthSessionUseCase,
    private val loginUseCase: LoginUseCase,
    private val loginWithTokenUseCase: LoginWithTokenUseCase,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BaseMviComponent<AuthScreenState, AuthScreenAction>(
    initialState = AuthScreenState(),
    coroutineScope = coroutineScope,
),
    AuthComponent,
    ComponentContext by componentContext {
    init {
        bootstrapPersistedSession()
    }

    override fun updateLogin(value: String) {
        onAction(AuthScreenAction.UpdateLogin(value))
    }

    override fun updatePassword(value: String) {
        onAction(AuthScreenAction.UpdatePassword(value))
    }

    override fun submit() {
        performSubmit()
    }

    override fun onAction(action: AuthScreenAction) {
        when (action) {
            is AuthScreenAction.UpdateLogin -> updateState(login = action.value)
            is AuthScreenAction.UpdatePassword -> updateState(password = action.value)
            AuthScreenAction.Submit -> performSubmit()
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

    private fun bootstrapPersistedSession() {
        val persistedSession = getPersistedAuthSessionUseCase() ?: return
        val loginToken = persistedSession.loginToken
        if (loginToken.isNullOrBlank()) {
            coroutineScope.launch {
                clearPersistedAuthSessionUseCase()
            }
            return
        }

        mutableState.value = mutableState.value.copy(
            isLoading = true,
            errorMessage = null,
            canSubmit = false,
        )

        coroutineScope.launch {
            runCatching {
                loginWithTokenUseCase(loginToken)
            }.onSuccess { session ->
                applyAuthorizedState(session)
            }.onFailure {
                clearPersistedAuthSessionUseCase()
                resetUnauthorizedState()
            }
        }
    }

    private fun performSubmit() {
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
                applyAuthorizedState(session)
            }.onFailure { throwable ->
                resetUnauthorizedState(
                    errorMessage = throwable.message ?: "Не удалось выполнить вход",
                )
            }
        }
    }

    private fun applyAuthorizedState(session: AuthSession) {
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
    }

    private fun resetUnauthorizedState(
        errorMessage: String? = null,
    ) {
        val currentState = mutableState.value
        mutableState.value = currentState.copy(
            isLoading = false,
            isAuthorized = false,
            errorMessage = errorMessage,
            sessionId = null,
            authorizedLogin = null,
            authorizedName = null,
            canSubmit = currentState.login.isNotBlank() && currentState.password.isNotBlank(),
        )
    }
}
