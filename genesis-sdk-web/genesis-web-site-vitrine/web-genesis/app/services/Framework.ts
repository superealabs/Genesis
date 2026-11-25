import { genesisApi } from './http'
import type { Language } from './Language'

export interface Framework {
  id: number; languageId: number; name: string; coreFramework: string; type: string
  isProd: boolean; useDB: boolean; useCloud: boolean; isGateway: boolean
  useFrontendApp: boolean; withGroupId?: boolean
}
export interface FrontendFramework {
  id: number; name: string; coreFramework: string; languageId: number
  defaultPort: string; type: string; viewExtension: string
}

export interface InterfaceLanguage { id: number; code: string; name: string }
export interface FrontendLayout {
  selectedInterfaceLanguages: string[]; navbarType: string; primaryColor: string; secondaryColor: string
  logoPath: string; faviconPath: string; port: string; logoAssetId: string; faviconAssetId: string
}

export const FrameworkService = {
  Getall: () => genesisApi<Framework[]>('/frameworks'),
  GetallFrameworkDisponible: async (language?: Language | number) => {
    const id = typeof language === 'number' ? language : language?.id
    return (await genesisApi<Framework[]>('/frameworks'))
      .filter(f => f.isProd && (id == null || f.languageId === id))
  },
  GetFrameworkByType_Framework: async (type: string) =>
    (await genesisApi<Framework[]>('/frameworks')).filter(f => f.type === type),
  GetallFrontend: () => genesisApi<FrontendFramework[]>('/frontend/frameworks'),
  Select: (id: number) => genesisApi(`/generator/frameworks/${id}/select`, { method: 'POST' }),
  ConfigureFrontend: (frameworkId: number, port: string) =>
    genesisApi('/generator/frontend-config', { method: 'POST', body: { frameworkId, languageId: null, port } }),
  GetViewTemplates: () => genesisApi<{ id: number; name: string }[]>('/frameworks/templates'),
  /** MVC : active le moteur de vues du framework (pas de port frontend séparé) */
  ConfigureMvcViews: (frameworkId: number) =>
    genesisApi('/generator/frontend-config', { method: 'POST', body: { frameworkId, languageId: null, port: null } }),
  // ── Réglage du layout (même étape que « Charte Graphique & Layout » de l'extension) ──
  GetInterfaceLanguages: () => genesisApi<InterfaceLanguage[]>('/frontend/interface-languages'),
  GetNavbarTypes: () => genesisApi<string[]>('/frontend/navbar-types'),
  /** Logo / favicon : PNG, JPEG ou ICO, 2 Mo max (favicon : PNG ou ICO). Renvoie l'assetId. */
  UploadAsset: async (kind: 'logo' | 'favicon', file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return (await genesisApi<{ assetId: string }>(`/generator/frontend/assets/${kind}`, { method: 'POST', body: fd })).assetId
  },
  ConfigureLayout: (layout: FrontendLayout) =>
    genesisApi('/generator/frontend-layout-config', { method: 'POST', body: layout })
}
