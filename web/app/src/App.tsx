import { useEffect, useMemo, useState } from 'react'
import { createFallbackState, getWebRootComponent, type AuthScreenState } from './bridge/kmp'

export function App() {
  const rootComponent = useMemo(() => getWebRootComponent(), [])
  const authComponent = rootComponent?.authComponent ?? null
  const [state, setState] = useState<AuthScreenState>(
    authComponent?.currentState() ?? createFallbackState(),
  )

  useEffect(() => {
    if (!authComponent) {
      return
    }

    const subscription = authComponent.watchState(setState)

    return () => {
      subscription.cancel()
    }
  }, [authComponent])

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
            if (authComponent) {
              authComponent.updateLogin(value)
              return
            }

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
            if (authComponent) {
              authComponent.updatePassword(value)
              return
            }

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
            if (authComponent) {
              authComponent.submit()
              return
            }

            setState((current) => ({
              ...current,
              errorMessage:
                'Shared KMP bridge не подключен. Пересоберите web bridge и перезапустите web shell.',
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
