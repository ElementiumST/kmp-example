import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import { initializeSharedCoreBridge } from './bridge/loadSharedCore'
import './styles.css'

async function bootstrap() {
  try {
    await initializeSharedCoreBridge()
  } catch (error) {
    console.error('Shared core bridge is unavailable:', error)
  }

  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <App />
    </React.StrictMode>,
  )
}

void bootstrap()
