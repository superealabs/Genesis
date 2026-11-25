import { genesisApi } from './http'
import type { Relation } from './Script'

export interface GenerationRequest {
  selectedTables?: string[]; selectedViews?: string[]; selectedComponents?: string[]; relations?: Relation[]
}
export interface GenerationResult { success: boolean; message: string; destination: string; tables: string[]; views: string[] }

export const GenerateService = {
  Reset: () => genesisApi('/generator/reset', { method: 'DELETE' }),
  Creation: (request: GenerationRequest = {}) =>
    genesisApi<GenerationResult>('/generator/script/launch', { method: 'POST', body: request })
}
