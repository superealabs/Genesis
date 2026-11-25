import { genesisApi } from './http'
export interface ScriptTables { success: boolean; database: string; tables: string[]; views: string[] }
export interface LlmModel { id: number; name: string; model: string }
export interface GenerateSqlRequest {
  databaseId: number; llmModelId: number | null; prompt: string
  currentScript: string; includeCurrentScript: boolean; apiKey: string | null
}
export interface GeneratedSql { success: boolean; database: string; model: string; script: string; tables: string[]; warning: string | null }
export interface Relation { parentTable: string; childTable: string; mandatory: boolean; hasForm: boolean }

/** Mode sans base de données : les tables sont lues dans le script SQL */
export const ScriptService = {
  Configure: (databaseId: number, script: string) =>
    genesisApi<ScriptTables>('/generator/script-database-config', { method: 'POST', body: { databaseId, script } }),
  GetTables: () => genesisApi<ScriptTables>('/generator/script/tables'),
  GetRelations: () => genesisApi<Relation[]>('/generator/script/relations'),
  /** Assistant IA (même client que le plugin IntelliJ : llm-api-config.json + genesis-core.properties) */
  GetLlmModels: () => genesisApi<LlmModel[]>('/generator/llm-models'),
  GenerateSql: (body: GenerateSqlRequest) =>
    genesisApi<GeneratedSql>('/generator/script/generate-sql', { method: 'POST', body })
}
