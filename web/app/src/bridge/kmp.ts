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
