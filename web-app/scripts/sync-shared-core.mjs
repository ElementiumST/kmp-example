import { cpSync, existsSync, mkdirSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const webAppDir = path.resolve(scriptDir, '..');
const workspaceDir = path.resolve(webAppDir, '..');
const sourceDir = path.resolve(
  workspaceDir,
  'kmp',
  'bridge-web',
  'build',
  'dist',
  'js',
  'developmentLibrary',
);
const targetDir = path.resolve(webAppDir, 'public', 'shared-core');
const gradleWrapper = path.resolve(workspaceDir, process.platform === 'win32' ? 'gradlew.bat' : 'gradlew');

const collectFiles = (directory) => {
  const entries = readdirSync(directory, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    const fullPath = path.resolve(directory, entry.name);
    if (entry.isDirectory()) {
      files.push(...collectFiles(fullPath));
    } else {
      files.push(fullPath);
    }
  }
  return files;
};

const parseDependencies = (filePath) => {
  const source = readFileSync(filePath, 'utf8');
  const match = source.match(/define\(\[([^\]]*)\],\s*factory\)/);
  if (!match) return [];
  return match[1]
    .split(',')
    .map((item) => item.trim().replace(/^['"]|['"]$/g, ''))
    .filter((item) => item.startsWith('./') && item.endsWith('.js'))
    .map((item) => item.slice(2));
};

const buildManifest = (directory) => {
  const jsFiles = collectFiles(directory)
    .filter((file) => file.endsWith('.js'))
    .map((file) => path.relative(directory, file).replaceAll('\\', '/'))
    .sort((a, b) => a.localeCompare(b));

  const graph = new Map();
  for (const file of jsFiles) {
    graph.set(file, parseDependencies(path.resolve(directory, file)));
  }

  const visiting = new Set();
  const visited = new Set();
  const result = [];

  const visit = (file) => {
    if (visited.has(file)) return;
    if (visiting.has(file)) return;
    visiting.add(file);
    const deps = graph.get(file) ?? [];
    for (const dep of deps) {
      if (graph.has(dep)) {
        visit(dep);
      }
    }
    visiting.delete(file);
    visited.add(file);
    result.push(file);
  };

  for (const file of jsFiles) {
    visit(file);
  }
  return result;
};

if (process.platform === 'win32') {
  execFileSync('cmd.exe', ['/c', gradleWrapper, ':kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution'], {
    cwd: workspaceDir,
    stdio: 'inherit',
  });
} else {
  execFileSync(gradleWrapper, [':kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution'], {
    cwd: workspaceDir,
    stdio: 'inherit',
  });
}

if (!existsSync(sourceDir)) {
  console.warn(
    '[sync-shared-core] Shared core bundle not found. Run "./gradlew :kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution" first.',
  );
  process.exit(0);
}

rmSync(targetDir, { recursive: true, force: true });
mkdirSync(targetDir, { recursive: true });
cpSync(sourceDir, targetDir, { recursive: true });

const files = buildManifest(targetDir);
writeFileSync(
  path.resolve(targetDir, 'manifest.json'),
  JSON.stringify({ files }, null, 2),
  'utf8',
);

console.log(`[sync-shared-core] Copied bundle to ${targetDir} (${files.length} scripts)`);
