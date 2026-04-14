export type AuthScreenState = {
  login: string
  password: string
  isLoading: boolean
  isAuthorized: boolean
  errorMessage: string | null
  sessionId: string | null
  authorizedLogin: string | null
  authorizedName: string | null
  submitLabel: string
  canSubmit: boolean
}

export type StateSubscription = {
  cancel: () => void
}

export type WebAuthComponent = {
  currentState: () => AuthScreenState
  watchState: (observer: (state: AuthScreenState) => void) => StateSubscription
  updateLogin: (value: string) => void
  updatePassword: (value: string) => void
  submit: () => void
}

export type WebRootComponent = {
  authComponent: WebAuthComponent
}

declare global {
  interface Window {
    SharedCoreBridge?: {
      createWebRootComponent: () => WebRootComponent
    }
  }
}

export function getWebRootComponent(): WebRootComponent | null {
  return window.SharedCoreBridge?.createWebRootComponent?.() ?? null
}

export function createFallbackState(): AuthScreenState {
  return {
    login: '',
    password: '',
    isLoading: false,
    isAuthorized: false,
    errorMessage: null,
    sessionId: null,
    authorizedLogin: null,
    authorizedName: null,
    submitLabel: 'Войти',
    canSubmit: false,
  }
}
