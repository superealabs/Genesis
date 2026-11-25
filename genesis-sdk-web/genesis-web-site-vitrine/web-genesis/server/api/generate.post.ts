// POST /api/generate  → génère le projet dans un dossier temporaire, renvoie le .zip, puis supprime le dossier.
// Ordre des appels genesis-api : reset > framework > script > projet > frontend > assets > layout > form-config > launch
import { mkdtempSync, mkdirSync, rmSync, readdirSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

interface GenerateBody {
  frameworkId: number
  databaseId: number
  script: string
  project: {
    projectName: string; groupId?: string; projectPort?: string
    languageVersion: string; frameworkVersion: string; buildTool: string; securityType?: string | null
  }
  frontend?: { frameworkId: number; port: string } | null
  /** Framework MVC : active le moteur de vues (requis pour régler leur layout) */
  mvcViews?: boolean
  /** Form configuration : { table: { colonne: { component, configuration } } } */
  formConfig?: Record<string, Record<string, unknown>> | null
  /** Réglage du layout (frontend-layout-config) ; logo / favicon en base64 */
  layout?: {
    selectedInterfaceLanguages: string[]; navbarType: string; primaryColor: string; secondaryColor: string; port?: string
    logo?: AssetFile | null; favicon?: AssetFile | null
  } | null
  selection: { selectedTables: string[]; selectedViews: string[]; selectedComponents: string[]; relations: unknown[] }
}

interface AssetFile { name: string; mime: string; data: string }

/** Envoie un logo / favicon à genesis-api et renvoie son assetId (vide si aucun fichier) */
async function uploadAsset(kind: 'logo' | 'favicon', file?: AssetFile | null): Promise<string> {
  if (!file?.data) return ''
  const fd = new FormData()
  fd.append('file', new Blob([Buffer.from(file.data, 'base64')], { type: file.mime }), file.name || kind)
  const r = await callGenesis<{ assetId: string }>(`/generator/frontend/assets/${kind}`, 'POST', fd)
  return r.assetId
}

// genesis-api garde UN SEUL contexte de génération en mémoire : une génération à la fois
let queue: Promise<unknown> = Promise.resolve()
function oneAtATime<T>(task: () => Promise<T>): Promise<T> {
  const run = queue.then(task, task)
  queue = run.catch(() => {})
  return run
}

export default defineEventHandler(async event => {
  const b = await readBody<GenerateBody>(event)
  if (!b?.frameworkId || !b?.databaseId || !b?.script || !b?.project?.projectName) {
    throw createError({ statusCode: 400, statusMessage: 'Framework, base, script et nom du projet obligatoires' })
  }

  // En ligne, le dossier doit être visible par genesis-api (volume Docker partagé, NUXT_WORK_DIR)
  const root = (useRuntimeConfig().workDir as string) || tmpdir()
  mkdirSync(root, { recursive: true })
  const workDir = mkdtempSync(join(root, 'genesis-'))
  return oneAtATime(async () => {
  try {
    await callGenesis('/generator/reset', 'DELETE').catch(() => {})
    await callGenesis(`/generator/frameworks/${b.frameworkId}/select`, 'POST')
    await callGenesis('/generator/script-database-config', 'POST', { databaseId: b.databaseId, script: b.script })
    await callGenesis('/generator/project-config', 'POST', {
      ...b.project,
      projectLocation: workDir,
      securityType: b.project.securityType || null
    })
    if (b.frontend) {
      await callGenesis('/generator/frontend-config', 'POST', { frameworkId: b.frontend.frameworkId, languageId: null, port: b.frontend.port })
    } else if (b.mvcViews) {
      await callGenesis('/generator/frontend-config', 'POST', { frameworkId: b.frameworkId, languageId: null, port: null })
    }
    if (b.layout && (b.frontend || b.mvcViews)) {
      // Après le reset (qui efface les fichiers transférés), juste avant le layout
      const logoAssetId = await uploadAsset('logo', b.layout.logo)
      const faviconAssetId = await uploadAsset('favicon', b.layout.favicon)
      await callGenesis('/generator/frontend-layout-config', 'POST', {
        selectedInterfaceLanguages: b.layout.selectedInterfaceLanguages,
        navbarType: b.layout.navbarType,
        primaryColor: b.layout.primaryColor,
        secondaryColor: b.layout.secondaryColor,
        logoPath: '', faviconPath: '',
        port: b.mvcViews ? '' : (b.layout.port || b.frontend?.port || ''),
        logoAssetId, faviconAssetId
      })
      if (b.formConfig && Object.keys(b.formConfig).length) {
        await callGenesis('/generator/form-config', 'PUT', b.formConfig)
      }
    }
    await callGenesis('/generator/script/launch', 'POST', b.selection)

    if (!readdirSync(workDir).length) {
      throw createError({ statusCode: 500, statusMessage: 'La génération n’a produit aucun fichier' })
    }
    const zip = zipFolder(workDir)
    setHeader(event, 'Content-Type', 'application/zip')
    setHeader(event, 'Content-Disposition', `attachment; filename="${b.project.projectName}.zip"`)
    return Buffer.from(zip)
  } finally {
    rmSync(workDir, { recursive: true, force: true })
  }
  })
})
