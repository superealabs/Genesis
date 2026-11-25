import { genesisApi } from './http'
export interface ProjectConfig {
  projectName: string; projectLocation: string; languageVersion: string; buildTool: string
  groupId: string; frameworkVersion: string; projectDescription?: string; projectPort: string
  securityType?: string | null
}

export const ProjectService = {
  GetLanguageVersions: (languageId: number) => genesisApi<string[]>(`/generator/language-versions/${languageId}`),
  GetFrameworkVersions: (frameworkId: number) => genesisApi<string[]>(`/generator/framework-versions/${frameworkId}`),
  GetBuildTools: (frameworkId: number) => genesisApi<string[]>(`/generator/build-tools/${frameworkId}`),
  GetSecurityTypes: (frameworkId: number) => genesisApi<string[]>(`/security_types/${frameworkId}`),
  Configure: (config: ProjectConfig) => genesisApi('/generator/project-config', { method: 'POST', body: config })
}
