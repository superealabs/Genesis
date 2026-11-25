// Étape « Form configuration » (FormConfigurationWizardStep du plugin IntelliJ)
// genesis-api : GET /api/frontend/form-field-configuration, GET /api/generator/form-config/tables,
//               PUT /api/generator/form-config
import { genesisApi } from './http'

export interface FormFieldOption { name: string; type: 'number' | 'select' | 'multi-select' | 'column' | string; options: string[] }
export interface FormCatalog { types: Record<string, string[]>; components: Record<string, FormFieldOption[]> }

export interface FormFieldValue { value: string; type?: string }
export interface FormFieldConfig { component: string; configuration: Record<string, FormFieldValue> }

export interface FormColumn {
  column: string            // nom SQL (clé de configuration)
  name: string
  sqlType: string
  category: string          // number | text | binary | json
  components: string[]
  coordinateColumns: string[]
  configured: FormFieldConfig | null
}
export interface FormTable { table: string; columns: FormColumn[]; hiddenColumns: number }

/** { table: { colonne: { component, configuration } } } */
export type FormConfigPayload = Record<string, Record<string, FormFieldConfig>>

export const FormConfigService = {
  GetCatalog: () => genesisApi<FormCatalog>('/frontend/form-field-configuration'),
  GetTables: (tables: string[]) =>
    genesisApi<FormTable[]>('/generator/form-config/tables' + (tables.length ? `?tables=${tables.map(encodeURIComponent).join(',')}` : '')),
  Save: (config: FormConfigPayload) => genesisApi('/generator/form-config', { method: 'PUT', body: config }),
  Clear: () => genesisApi('/generator/form-config', { method: 'DELETE' })
}
