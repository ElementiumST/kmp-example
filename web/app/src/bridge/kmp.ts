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

export type WebRootController = {
  currentState: () => AuthScreenState
  subscribeState: (observer: (state: AuthScreenState) => void) => void
  clearSubscription: () => void
  updateLogin: (value: string) => void
  updatePassword: (value: string) => void
  submit: () => void
}

declare global {
  interface Window {
    SharedCoreBridge?: {
      createWebRootController: () => WebRootController
    }
  }
}

export function getWebRootController(): WebRootController | null {
  return window.SharedCoreBridge?.createWebRootController?.() ?? null
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
