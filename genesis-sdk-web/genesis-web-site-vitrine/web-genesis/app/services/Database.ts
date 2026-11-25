import { genesisApi } from './http'
export interface DatabaseEngine { id: number; name: string; port: string; driver?: string }

export const DatabaseService = {
  Getall: () => genesisApi<DatabaseEngine[]>('/databases')
}
