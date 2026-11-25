import { genesisApi } from './http'
export interface Language { id: number; name: string; extension?: string }

export const LanguageService = {
  Getall: () => genesisApi<Language[]>('/frameworks/languages')
}
