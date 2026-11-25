import { FrameworkService, type Framework, type FrontendFramework } from '~/services/Framework'
import { LanguageService, type Language } from '~/services/Language'
import { DatabaseService, type DatabaseEngine } from '~/services/Database'

/** Catalogues genesis-api (chargés côté navigateur : l'API tourne en local). */
export function useGenesisCatalog() {
  return useLazyAsyncData('genesis-catalog', async () => {
    const [frameworks, languages, frontends, databases] = await Promise.all([
      FrameworkService.Getall(), LanguageService.Getall(), FrameworkService.GetallFrontend(), DatabaseService.Getall()
    ])
    return { frameworks, languages, frontends, databases } as {
      frameworks: Framework[]; languages: Language[]; frontends: FrontendFramework[]; databases: DatabaseEngine[]
    }
  }, { server: false })
}
