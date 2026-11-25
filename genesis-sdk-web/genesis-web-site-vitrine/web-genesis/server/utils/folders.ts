// Navigation dans les dossiers de la machine (celle qui fait tourner le site et genesis-api en local).
// Le navigateur ne peut pas donner le chemin complet d'un dossier : c'est le serveur Nuxt qui le fournit.
import { existsSync, readdirSync, statSync, mkdirSync } from 'node:fs'
import { homedir, platform } from 'node:os'
import { dirname, join, resolve, basename } from 'node:path'

export interface FolderEntry { name: string; path: string }

function roots(): FolderEntry[] {
  if (platform() !== 'win32') return [{ name: '/', path: '/' }]
  // Lecteurs Windows présents (C:\, D:\ ...)
  return 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('')
    .map(l => `${l}:\\`)
    .filter(p => existsSync(p))
    .map(p => ({ name: p, path: p }))
}

export function listFolders(requested?: string) {
  const dir = resolve(requested?.trim() || homedir())
  if (!existsSync(dir) || !statSync(dir).isDirectory()) {
    throw createError({ statusCode: 404, statusMessage: `Dossier introuvable : ${dir}` })
  }
  let folders: FolderEntry[] = []
  try {
    folders = readdirSync(dir, { withFileTypes: true })
      .filter(d => d.isDirectory() && !d.name.startsWith('.') && !d.name.startsWith('$'))
      .map(d => ({ name: d.name, path: join(dir, d.name) }))
      .sort((a, b) => a.name.localeCompare(b.name, 'fr', { sensitivity: 'base' }))
  } catch {
    throw createError({ statusCode: 403, statusMessage: `Accès refusé : ${dir}` })
  }
  const parent = dirname(dir)
  return { path: dir, parent: parent === dir ? null : parent, home: homedir(), roots: roots(), folders }
}

export function createFolder(parent: string, name: string) {
  const clean = (name || '').trim()
  if (!parent || !clean || /[\\/]/.test(clean) || clean === '.' || clean === '..') {
    throw createError({ statusCode: 400, statusMessage: `Nom de dossier invalide : ${name}` })
  }
  const target = join(resolve(parent), basename(clean))
  try {
    mkdirSync(target, { recursive: true })
  } catch {
    throw createError({ statusCode: 403, statusMessage: `Impossible de créer : ${target}` })
  }
  return listFolders(target)
}

/** Interdit en ligne (NUXT_PUBLIC_ALLOW_LOCAL_FOLDERS=false) : on ne montre pas les dossiers du serveur */
export function assertLocalFoldersAllowed() {
  if (!useRuntimeConfig().public.allowLocalFolders) {
    throw createError({ statusCode: 403, statusMessage: 'Choix d’un dossier désactivé sur ce serveur : utilisez le téléchargement .zip' })
  }
}
