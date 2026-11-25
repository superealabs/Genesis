// État et logique du Playground (mode script : génération sans base de données).
// Ordre des appels genesis-api : Reset > Select framework > Script > Project > Frontend > Assets > Layout > Form config > Creation
import { FrameworkService, type Framework, type FrontendFramework, type InterfaceLanguage, type FrontendLayout } from '~/services/Framework'
import { FormConfigService, type FormCatalog, type FormTable, type FormConfigPayload } from '~/services/FormConfig'
import { LanguageService, type Language } from '~/services/Language'
import { DatabaseService, type DatabaseEngine } from '~/services/Database'
import { ProjectService } from '~/services/Project'
import { ScriptService, type Relation, type LlmModel } from '~/services/Script'
import { GenerateService, type GenerationResult } from '~/services/Generate'

export const STEPS = ['Framework', 'Projet', 'Base de données', 'Script SQL', 'Relations', 'Frontend', 'Formulaires', 'Résumé'] as const
/** Étape « Formulaires » (Form configuration) : seulement avec un frontend ou des vues MVC */
export const FORM_STEP = 7

export interface FormColumnState {
  component: string
  /** paramètres du composant ; multi-select = tableau */
  values: Record<string, string | string[]>
  /** composant map : rôle de cette colonne (l'autre coordonnée est dans values) */
  role?: 'latitude' | 'longitude'
}

/** Script d'exemple écrit avec les types de chaque base (databases.json) */
export const EXAMPLE_SQL: Record<string, string> = {
  PostgreSQL: `CREATE TABLE client (
  id SERIAL PRIMARY KEY,
  nom VARCHAR(100) NOT NULL,
  email VARCHAR(150) UNIQUE
);

CREATE TABLE commande (
  id SERIAL PRIMARY KEY,
  date_commande DATE,
  montant NUMERIC(10,2),
  client_id INT NOT NULL REFERENCES client(id)
);`,
  MySQL: `CREATE TABLE client (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(100) NOT NULL,
  email VARCHAR(150) UNIQUE
);

CREATE TABLE commande (
  id INT AUTO_INCREMENT PRIMARY KEY,
  date_commande DATE,
  montant DECIMAL(10,2),
  client_id INT NOT NULL,
  FOREIGN KEY (client_id) REFERENCES client(id)
);`,
  'SQL Server': `CREATE TABLE client (
  id INT IDENTITY(1,1) PRIMARY KEY,
  nom NVARCHAR(100) NOT NULL,
  email NVARCHAR(150) UNIQUE
);

CREATE TABLE commande (
  id INT IDENTITY(1,1) PRIMARY KEY,
  date_commande DATE,
  montant DECIMAL(10,2),
  client_id INT NOT NULL REFERENCES client(id)
);`,
  Oracle: `CREATE TABLE client (
  id NUMBER(10) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nom VARCHAR2(100) NOT NULL,
  email VARCHAR2(150) UNIQUE
);

CREATE TABLE commande (
  id NUMBER(10) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  date_commande DATE,
  montant NUMBER(10,2),
  client_id NUMBER(10) NOT NULL REFERENCES client(id)
);`
}
const isExample = (sql: string) => !sql.trim() || Object.values(EXAMPLE_SQL).some(e => e.trim() === sql.trim())

export interface AiMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  /** SQL proposé par l'IA (réponse assistant) */
  script?: string
  tables?: string[]
  warning?: string | null
  error?: boolean
  loading?: boolean
  at: Date
}


function initial() {
  return {
    step: 1,
    visited: [1] as number[],
    error: '',
    loading: false,
    // catalogues
    languages: [] as Language[],
    frameworks: [] as Framework[],
    databases: [] as DatabaseEngine[],
    frontends: [] as FrontendFramework[],
    interfaceLanguages: [] as InterfaceLanguage[],
    navbarTypes: [] as string[],
    options: { languageVersions: [] as string[], frameworkVersions: [] as string[], buildTools: [] as string[], securityTypes: [] as string[] },
    // choix
    framework: null as Framework | null,
    database: null as DatabaseEngine | null,
    frontend: null as FrontendFramework | null,
    withFrontend: true,
    output: 'zip' as 'zip' | 'folder',     // zip = dossier temporaire puis téléchargement
    downloadUrl: '',
    frontPort: '5173',
    // Réglage du layout (POST /api/generator/frontend-layout-config), valeurs par défaut de l'extension
    layout: {
      selectedInterfaceLanguages: ['fr'] as string[],
      navbarType: 'side',
      primaryColor: '#3B82F6',
      secondaryColor: '#64748B'
    },
    // Form configuration : rendu de chaque colonne dans les formulaires générés
    formCatalog: null as FormCatalog | null,
    formTables: [] as FormTable[],
    formTable: '',
    /** table -> colonne SQL -> { component, values } ; component vide = rendu par défaut */
    formConfig: {} as Record<string, Record<string, FormColumnState>>,
    logoFile: null as File | null,
    faviconFile: null as File | null,
    logoPreview: '',
    faviconPreview: '',
    project: {
      projectName: 'MonProjet', groupId: 'com.genesis', projectLocation: '', projectPort: '8080',
      languageVersion: '', frameworkVersion: '', buildTool: '', securityType: ''
    },
    script: EXAMPLE_SQL.PostgreSQL,
    tables: [] as string[],
    views: [] as string[],
    selectedTables: [] as string[],
    selectedViews: [] as string[],
    components: ['model', 'dao', 'service', 'controller'] as string[],
    relations: [] as Relation[],
    // Assistant IA du script (POST /api/generator/script/generate-sql)
    ai: {
      models: [] as LlmModel[],
      modelId: null as number | null,
      includeCurrentScript: true,
      useToken: false,
      token: '',
      prompt: '',
      busy: false,
      messages: [] as AiMessage[]
    },
    result: null as GenerationResult | null
  }
}

export function usePlayground() {
  const s = useState('playground', () => initial())
  const isMvc = computed(() => s.value.framework?.type === 'MVC')
  const hasFront = computed(() => !!s.value.framework?.useFrontendApp && !isMvc.value && s.value.withFrontend)
  /** Le layout s'applique au frontend séparé (SPA) ou aux vues MVC */
  const hasLayout = computed(() => isMvc.value || hasFront.value)

  async function guard<T>(fn: () => Promise<T>): Promise<T | undefined> {
    s.value.error = ''
    try { return await fn() } catch (e: any) { s.value.error = e?.message || String(e) }
  }

  /** Hooks par étape : onEnter (chargement) et beforeNext (validation + appel API) */
  const hooks: Record<number, { onEnter?: () => Promise<void>; beforeNext?: () => Promise<boolean> }> = {
    1: {
      onEnter: async () => { await guard(async () => {
        if (!s.value.frameworks.length) {
          const [langs, fws] = await Promise.all([LanguageService.Getall(), FrameworkService.GetallFrameworkDisponible()])
          s.value.languages = langs; s.value.frameworks = fws
        }
      }) },
      beforeNext: async () => {
        if (!s.value.framework) { s.value.error = 'Choisissez un framework.'; return false }
        return (await guard(() => FrameworkService.Select(s.value.framework!.id))) !== undefined
      }
    },
    2: {
      onEnter: async () => { await guard(async () => {
        const fw = s.value.framework!
        const [lv, fv, bt, st] = await Promise.all([
          ProjectService.GetLanguageVersions(fw.languageId), ProjectService.GetFrameworkVersions(fw.id),
          ProjectService.GetBuildTools(fw.id), ProjectService.GetSecurityTypes(fw.id).catch(() => [])
        ])
        s.value.options = { languageVersions: lv, frameworkVersions: fv, buildTools: bt, securityTypes: st }
        const p = s.value.project
        if (!lv.includes(p.languageVersion)) p.languageVersion = lv.at(-1) ?? ''
        if (!fv.includes(p.frameworkVersion)) p.frameworkVersion = fv[0] ?? ''
        if (!bt.includes(p.buildTool)) p.buildTool = bt[0] ?? ''
      }) },
      beforeNext: async () => {
        const p = s.value.project
        if (!/^[A-Za-z][A-Za-z0-9]*$/.test(p.projectName)) { s.value.error = 'Nom du projet : une lettre puis des lettres ou chiffres, sans espace.'; return false }
        if (s.value.output === 'folder' && !p.projectLocation.trim()) { s.value.error = 'Choisissez le dossier de destination (icône dossier).'; return false }
        return true
      }
    },
    3: {
      onEnter: async () => { await guard(async () => {
        if (!s.value.databases.length) s.value.databases = await DatabaseService.Getall()
      }) },
      beforeNext: async () => {
        if (!s.value.database) { s.value.error = 'Choisissez une base de données.'; return false }
        return true
      }
    },
    4: {
      onEnter: async () => {
        if (s.value.ai.models.length) return
        try {
          s.value.ai.models = await ScriptService.GetLlmModels()
          s.value.ai.modelId ??= s.value.ai.models[0]?.id ?? null
        } catch { /* assistant IA indisponible : le reste de l'étape fonctionne */ }
      },
      beforeNext: async () => {
        if (!s.value.script.trim()) { s.value.error = 'Collez un script SQL (CREATE TABLE ...).'; return false }
        if (!s.value.tables.length && !s.value.views.length) await analyseScript()
        if (!s.value.tables.length && !s.value.views.length) return false
        if (!s.value.selectedTables.length && !s.value.selectedViews.length) { s.value.error = 'Cochez au moins une table.'; return false }
        if (!s.value.components.length) { s.value.error = 'Cochez au moins un composant.'; return false }
        return true
      }
    },
    5: {
      onEnter: async () => { await guard(async () => {
        await FrameworkService.Select(s.value.framework!.id)
        await ScriptService.Configure(s.value.database!.id, s.value.script)
        const detected = await ScriptService.GetRelations()
        // garde les réglages déjà faits par l'utilisateur
        const prev = new Map(s.value.relations.map(r => [`${r.parentTable}>${r.childTable}`, r]))
        s.value.relations = detected
          .filter(r => s.value.selectedTables.includes(r.parentTable) && s.value.selectedTables.includes(r.childTable))
          .map(r => prev.get(`${r.parentTable}>${r.childTable}`) ?? r)
      }) }
    },
    6: {
      onEnter: async () => { await guard(async () => {
        const [fronts, langs, navs] = await Promise.all([
          s.value.frontends.length ? Promise.resolve(s.value.frontends) : FrameworkService.GetallFrontend().then(l => l.filter(f => f.type === 'spa')),
          s.value.interfaceLanguages.length ? Promise.resolve(s.value.interfaceLanguages) : FrameworkService.GetInterfaceLanguages(),
          s.value.navbarTypes.length ? Promise.resolve(s.value.navbarTypes) : FrameworkService.GetNavbarTypes()
        ])
        s.value.frontends = fronts; s.value.interfaceLanguages = langs; s.value.navbarTypes = navs
        if (!s.value.frontend) s.value.frontend = s.value.frontends[0] ?? null
        const codes = langs.map(l => l.code)
        s.value.layout.selectedInterfaceLanguages = s.value.layout.selectedInterfaceLanguages.filter(c => codes.includes(c))
        if (!s.value.layout.selectedInterfaceLanguages.length && codes.length) s.value.layout.selectedInterfaceLanguages = [codes.includes('fr') ? 'fr' : codes[0]!]
        if (navs.length && !navs.includes(s.value.layout.navbarType)) s.value.layout.navbarType = navs[0]!
      }) },
      beforeNext: async () => {
        if (hasFront.value && !s.value.frontend) { s.value.error = 'Choisissez un frontend ou « Sans frontend ».'; return false }
        if (hasLayout.value) {
          const l = s.value.layout
          if (!l.selectedInterfaceLanguages.length) { s.value.error = 'Sélectionnez au moins une langue d’interface.'; return false }
          if (!l.navbarType) { s.value.error = 'Choisissez le type de navbar.'; return false }
          const hex = /^#[0-9a-fA-F]{6}$/
          if (!hex.test(l.primaryColor)) { s.value.error = 'Couleur principale invalide (format #RRGGBB).'; return false }
          if (!hex.test(l.secondaryColor)) { s.value.error = 'Couleur secondaire invalide (format #RRGGBB).'; return false }
        }
        return true
      }
    },
    7: {
      onEnter: async () => {
        if (!hasLayout.value) return
        await guard(async () => {
          if (!s.value.formCatalog) s.value.formCatalog = await FormConfigService.GetCatalog()
          // le contexte de l'API a pu être réinitialisé (génération précédente) : on le refait
          await FrameworkService.Select(s.value.framework!.id)
          await ScriptService.Configure(s.value.database!.id, s.value.script)
          s.value.formTables = await FormConfigService.GetTables(s.value.selectedTables)
          // reprend une configuration déjà renvoyée par l'API (ou faite plus tôt) sans l'écraser
          for (const t of s.value.formTables) {
            const conf = (s.value.formConfig[t.table] ??= {})
            for (const c of t.columns) {
              if (conf[c.column] || !c.configured) continue
              conf[c.column] = fromApi(c.configured)
            }
          }
          if (!s.value.formTables.some(t => t.table === s.value.formTable)) s.value.formTable = s.value.formTables[0]?.table ?? ''
        })
      },
      beforeNext: async () => {
        if (!hasLayout.value) return true
        // validation par genesis-api (mêmes règles que le plugin) ; renvoyée après le reset à la génération
        return (await guard(() => FormConfigService.Save(formConfigPayload()))) !== undefined
      }
    }
  }

  /** Configuration de l'API -> état du formulaire */
  function fromApi(c: { component: string; configuration: Record<string, { value: string }> }): FormColumnState {
    const values: Record<string, string | string[]> = {}
    let role: FormColumnState['role']
    const fields = s.value.formCatalog?.components[c.component] ?? []
    for (const [k, v] of Object.entries(c.configuration ?? {})) {
      const spec = fields.find(f => f.name === k)
      values[k] = spec?.type === 'multi-select' ? v.value.split(',').filter(Boolean) : v.value
    }
    if (c.component === 'map') role = values.latitude ? 'longitude' : 'latitude'
    return { component: c.component, values, role }
  }

  /** État du formulaire -> corps de PUT /api/generator/form-config */
  function formConfigPayload(): FormConfigPayload {
    const out: FormConfigPayload = {}
    for (const [table, cols] of Object.entries(s.value.formConfig)) {
      if (!s.value.selectedTables.includes(table)) continue
      for (const [column, st] of Object.entries(cols)) {
        if (!st.component) continue
        const fields = s.value.formCatalog?.components[st.component] ?? []
        const configuration: Record<string, { value: string; type: string }> = {}
        for (const f of fields) {
          if (st.component === 'map' && (f.name === 'latitude' || f.name === 'longitude')) {
            // la colonne courante joue « role » ; on n'envoie que l'autre coordonnée
            const other = st.role === 'latitude' ? 'longitude' : 'latitude'
            if (f.name === other && st.values[other]) configuration[other] = { value: String(st.values[other]), type: f.type }
            continue
          }
          const v = st.values[f.name]
          const value = Array.isArray(v) ? v.join(',') : (v ?? '').toString().trim()
          if (value) configuration[f.name] = { value, type: f.type }
        }
        ;(out[table] ??= {})[column] = { component: st.component, configuration }
      }
    }
    return out
  }

  async function analyseScript() {
    await guard(async () => {
      const r = await ScriptService.Configure(s.value.database!.id, s.value.script)
      s.value.tables = r.tables; s.value.views = r.views
      s.value.selectedTables = s.value.selectedTables.filter(t => r.tables.includes(t))
      if (!s.value.selectedTables.length) s.value.selectedTables = [...r.tables]
      s.value.selectedViews = s.value.selectedViews.filter(v => r.views.includes(v))
    })
  }

  /** Choix de la base : l'exemple non modifié est remplacé par celui de la nouvelle base */
  function selectDatabase(db: DatabaseEngine) {
    s.value.database = db
    if (isExample(s.value.script)) s.value.script = EXAMPLE_SQL[db.name] ?? s.value.script
    s.value.tables = []; s.value.views = []
    if (s.value.error.includes('ne correspond pas')) s.value.error = ''
  }

  /** Bases proposées par genesis-api quand le script ne correspond pas (« ... correspond plutôt à : X, Y. ») */
  const suggestedDatabases = computed(() => {
    const m = s.value.error.match(/correspond plutôt à : (.+?)\.?$/)
    if (!m) return [] as DatabaseEngine[]
    const names = m[1]!.split(',').map(n => n.trim())
    return s.value.databases.filter(d => names.includes(d.name))
  })

  let aiSeq = 0
  /** Assistant IA : génère un script pour la base choisie (le script n'est appliqué qu'au clic) */
  async function askAi() {
    const ai = s.value.ai
    const prompt = ai.prompt.trim()
    if (!prompt || ai.busy || !s.value.database) return
    ai.prompt = ''
    ai.messages.push({ id: ++aiSeq, role: 'user', content: prompt, at: new Date() })
    const answer: AiMessage = { id: ++aiSeq, role: 'assistant', content: '', loading: true, at: new Date() }
    ai.messages.push(answer)
    const msg = ai.messages[ai.messages.length - 1]!
    ai.busy = true
    try {
      const r = await ScriptService.GenerateSql({
        databaseId: s.value.database.id, llmModelId: ai.modelId, prompt,
        currentScript: s.value.script, includeCurrentScript: ai.includeCurrentScript,
        apiKey: ai.useToken && ai.token.trim() ? ai.token.trim() : null
      })
      msg.content = `${r.tables.length} table(s) pour ${r.database} — ${r.model}`
      msg.script = r.script; msg.tables = r.tables; msg.warning = r.warning
    } catch (e: any) {
      msg.content = e?.message || String(e); msg.error = true
    } finally {
      msg.loading = false; ai.busy = false
    }
  }

  /** Place le SQL de l'IA dans l'éditeur (remplace ou ajoute) puis relance l'analyse */
  async function applyAiScript(script: string, mode: 'replace' | 'append') {
    s.value.script = mode === 'replace' ? script : `${s.value.script.trimEnd()}\n\n${script}`
    await nextTick()
    await analyseScript()
  }

  async function goTo(step: number) {
    s.value.error = ''
    s.value.step = step
    if (!s.value.visited.includes(step)) s.value.visited.push(step)
    await hooks[step]?.onEnter?.()
  }

  async function next() {
    if (s.value.step === STEPS.length) return generate()
    const ok = await (hooks[s.value.step]?.beforeNext?.() ?? Promise.resolve(true))
    if (!ok) return
    const target = s.value.step + 1
    await goTo(target === FORM_STEP && !hasLayout.value ? target + 1 : target)
  }

  async function previous() {
    if (s.value.step <= 1) return
    const target = s.value.step - 1
    await goTo(target === FORM_STEP && !hasLayout.value ? target - 1 : target)
  }

  /** Logo / favicon : mêmes règles que genesis-api (PNG, JPEG ou ICO, 2 Mo ; favicon sans JPEG) */
  function setAsset(kind: 'logo' | 'favicon', file: File | null) {
    s.value.error = ''
    const previewKey = kind === 'logo' ? 'logoPreview' : 'faviconPreview'
    if (s.value[previewKey]) URL.revokeObjectURL(s.value[previewKey])
    s.value[previewKey] = ''
    if (file) {
      const okTypes = kind === 'logo'
        ? ['image/png', 'image/jpeg', 'image/x-icon', 'image/vnd.microsoft.icon']
        : ['image/png', 'image/x-icon', 'image/vnd.microsoft.icon']
      const okExt = kind === 'logo' ? /\.(png|jpe?g|ico)$/i : /\.(png|ico)$/i
      if (!okTypes.includes(file.type) && !okExt.test(file.name)) {
        s.value.error = kind === 'logo' ? 'Logo : PNG, JPEG ou ICO uniquement.' : 'Favicon : PNG ou ICO uniquement.'
        file = null
      } else if (file.size > 2 * 1024 * 1024) {
        s.value.error = 'Image supérieure à 2 Mo.'
        file = null
      }
    }
    if (kind === 'logo') s.value.logoFile = file
    else s.value.faviconFile = file
    if (file) s.value[previewKey] = URL.createObjectURL(file)
  }

  /** Corps envoyé à frontend-layout-config (port vide en MVC : les vues utilisent le port du backend) */
  function layoutBody(logoAssetId = '', faviconAssetId = ''): FrontendLayout {
    const l = s.value.layout
    return {
      selectedInterfaceLanguages: [...l.selectedInterfaceLanguages], navbarType: l.navbarType,
      primaryColor: l.primaryColor, secondaryColor: l.secondaryColor,
      logoPath: '', faviconPath: '', port: isMvc.value ? '' : s.value.frontPort,
      logoAssetId, faviconAssetId
    }
  }

  async function fileToBase64(file: File | null) {
    if (!file) return null
    const buf = new Uint8Array(await file.arrayBuffer())
    let bin = ''
    for (let i = 0; i < buf.length; i += 0x8000) bin += String.fromCharCode(...buf.subarray(i, i + 0x8000))
    return { name: file.name, mime: file.type || 'application/octet-stream', data: btoa(bin) }
  }

  /** Mode zip : le serveur du site génère dans un dossier temporaire et renvoie le .zip */
  async function generateZip() {
    const st = s.value
    try {
      const blob = await $fetch<Blob>('/api/generate', {
        method: 'POST',
        responseType: 'blob',
        body: {
          frameworkId: st.framework!.id,
          databaseId: st.database!.id,
          script: st.script,
          project: {
            projectName: st.project.projectName, groupId: st.project.groupId, projectPort: st.project.projectPort,
            languageVersion: st.project.languageVersion, frameworkVersion: st.project.frameworkVersion,
            buildTool: st.project.buildTool, securityType: st.project.securityType || null
          },
          frontend: hasFront.value ? { frameworkId: st.frontend!.id, port: st.frontPort } : null,
          mvcViews: isMvc.value,
          formConfig: hasLayout.value ? formConfigPayload() : null,
          layout: hasLayout.value ? {
            ...layoutBody(),
            logo: await fileToBase64(st.logoFile),
            favicon: await fileToBase64(st.faviconFile)
          } : null,
          selection: {
            selectedTables: st.selectedTables, selectedViews: st.selectedViews,
            selectedComponents: st.components, relations: st.relations
          }
        }
      })
      if (st.downloadUrl) URL.revokeObjectURL(st.downloadUrl)
      st.downloadUrl = URL.createObjectURL(blob)
      download()
      return {
        success: true, message: `Projet généré et téléchargé (${Math.round(blob.size / 1024)} Ko)`,
        destination: `${st.project.projectName}.zip`, tables: st.selectedTables, views: st.selectedViews
      } as GenerationResult
    } catch (e: any) {
      let msg = e?.message || String(e)
      const d = e?.data
      try {
        const j = d instanceof Blob ? JSON.parse(await d.text()) : d
        msg = j?.statusMessage || j?.message || msg
      } catch { /* réponse non JSON */ }
      s.value.error = msg
    }
  }

  /** Relance le téléchargement du dernier .zip */
  function download() {
    const a = document.createElement('a')
    a.href = s.value.downloadUrl
    a.download = `${s.value.project.projectName}.zip`
    a.click()
  }

  async function generate() {
    s.value.loading = true
    s.value.result = null
    s.value.error = ''
    if (s.value.output === 'zip') {
      const r = await generateZip()
      s.value.loading = false
      if (r) s.value.result = r
      return
    }
    const st = s.value
    const res = await guard(async () => {
      await GenerateService.Reset().catch(() => {})
      await FrameworkService.Select(st.framework!.id)
      await ScriptService.Configure(st.database!.id, st.script)
      await ProjectService.Configure({
        projectName: st.project.projectName, projectLocation: st.project.projectLocation,
        languageVersion: st.project.languageVersion, buildTool: st.project.buildTool,
        groupId: st.project.groupId, frameworkVersion: st.project.frameworkVersion,
        projectPort: st.project.projectPort, securityType: st.project.securityType || null
      })
      if (hasFront.value) await FrameworkService.ConfigureFrontend(st.frontend!.id, st.frontPort)
      else if (isMvc.value) await FrameworkService.ConfigureMvcViews(st.framework!.id)
      if (hasLayout.value) {
        // Les fichiers sont envoyés après le reset (qui les efface), juste avant le layout
        const logoId = st.logoFile ? await FrameworkService.UploadAsset('logo', st.logoFile) : ''
        const faviconId = st.faviconFile ? await FrameworkService.UploadAsset('favicon', st.faviconFile) : ''
        await FrameworkService.ConfigureLayout(layoutBody(logoId, faviconId))
        // Form configuration : renvoyée après le reset (qui l'efface)
        const formConfig = formConfigPayload()
        if (Object.keys(formConfig).length) await FormConfigService.Save(formConfig)
      }
      return GenerateService.Creation({
        selectedTables: st.selectedTables, selectedViews: st.selectedViews,
        selectedComponents: st.components, relations: st.relations
      })
    })
    s.value.loading = false
    if (res) s.value.result = res
  }

  function reset() {
    if (s.value.downloadUrl) URL.revokeObjectURL(s.value.downloadUrl)
    if (s.value.logoPreview) URL.revokeObjectURL(s.value.logoPreview)
    if (s.value.faviconPreview) URL.revokeObjectURL(s.value.faviconPreview)
    s.value = initial()
  }

  return { s, isMvc, hasFront, hasLayout, next, previous, goTo, generate, reset, analyseScript, download, setAsset, formConfigPayload, selectDatabase, suggestedDatabases, askAi, applyAiScript }
}
