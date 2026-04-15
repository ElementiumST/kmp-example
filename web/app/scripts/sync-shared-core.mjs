import { cpSync, existsSync, mkdirSync, rmSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const scriptDir = path.dirname(fileURLToPath(import.meta.url))
const webAppDir = path.resolve(scriptDir, '..')
const workspaceDir = path.resolve(webAppDir, '..', '..')
const sourceDir = path.resolve(webAppDir, '..', '..', 'kmp', 'core', 'build', 'dist', 'js', 'developmentLibrary')
const targetDir = path.resolve(webAppDir, 'public', 'shared-core')
const gradleWrapper = path.resolve(workspaceDir, process.platform === 'win32' ? 'gradlew.bat' : 'gradlew')

if (process.platform === 'win32') {
  execFileSync('cmd.exe', ['/c', gradleWrapper, ':kmp:core:jsBrowserDevelopmentLibraryDistribution'], {
    cwd: workspaceDir,
    stdio: 'inherit',
  })
} else {
  execFileSync(gradleWrapper, [':kmp:core:jsBrowserDevelopmentLibraryDistribution'], {
    cwd: workspaceDir,
    stdio: 'inherit',
  })
}

if (!existsSync(sourceDir)) {
  console.warn(
    '[sync-shared-core] Shared core bundle not found. Run "./gradlew :kmp:core:jsBrowserDevelopmentLibraryDistribution" first.',
  )
  process.exit(0)
}

rmSync(targetDir, { recursive: true, force: true })
mkdirSync(targetDir, { recursive: true })
cpSync(sourceDir, targetDir, { recursive: true })

console.log(`[sync-shared-core] Copied bundle to ${targetDir}`)
