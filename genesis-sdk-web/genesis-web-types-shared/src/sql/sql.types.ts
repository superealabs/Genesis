// ═══ SQL SCRIPT DTOs (miroir de SqlScriptDtos.java) ═══

export interface SqlScriptRequest {
  script: string;
  confirmExecution?: boolean;
}

export interface ScriptConfigResponse {
  success: boolean;
  message: string;
}

export interface SqlValidationResponse {
  valid: boolean;
  message: string;
}

export interface SqlExecutionResponse {
  success: boolean;
  message: string;
}

// ScriptConfigRequest / ScriptConfigResponse : miroir existant
// (déjà mappés côté TS par `ScriptConfig` — vérifie la cohérence)
export interface ScriptConfig {
  path: string;
  content: string;
}