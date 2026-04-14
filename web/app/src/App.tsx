import { useState } from 'react'
import { createFallbackState, type AuthScreenState } from './bridge/kmp'

export function App() {
  const authComponent = null
  const [state, setState] = useState<AuthScreenState>(
    createFallbackState(),
  )

  return (
    <main className="app-shell">
      <section className="card">
        {state.isLoading ? <div className="loader" aria-label="Loading shared state" /> : null}
        <h1>Авторизация</h1>

        <input
          type="text"
          value={state.login}
          placeholder="Логин"
          onChange={(event) => {
            const value = event.target.value
            setState((current) => ({
              ...current,
              login: value,
              canSubmit: value.trim().length > 0 && current.password.trim().length > 0,
            }))
          }}
        />

        <input
          type="password"
          value={state.password}
          placeholder="Пароль"
          onChange={(event) => {
            const value = event.target.value
            setState((current) => ({
              ...current,
              password: value,
              canSubmit: current.login.trim().length > 0 && value.trim().length > 0,
            }))
          }}
        />

        <button
          type="button"
          disabled={!state.canSubmit}
          onClick={() => {
            setState((current) => ({
              ...current,
              errorMessage:
                'Shared auth-logic для web сейчас не подключена. Используется локальный fallback UI.',
            }))
          }}
        >
          {state.submitLabel}
        </button>

        {state.errorMessage ? <p className="error-text">{state.errorMessage}</p> : null}

        {state.isAuthorized ? (
          <div className="success-box">
            <p>Вход выполнен: {state.authorizedName ?? state.authorizedLogin}</p>
            {state.sessionId ? <p>Session: {state.sessionId}</p> : null}
          </div>
        ) : null}

        {!authComponent ? (
          <p className="hint-text">
            Web UI поднят в fallback-режиме. После подключения KMP bridge форма начнет работать через
            shared state.
          </p>
        ) : null}
      </section>
    </main>
  )
}

export default App
