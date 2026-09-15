export interface DatabaseEngineDto {
    id: number;
    name: string;
    driver: string;
    driverName: string;
    port: string;
    driverType?: string;
    sid?: string;
    driverVersion?: string;
    databaseMajorVersion?: number;
}
export interface DatabaseConfig {
    engine: 'mysql' | 'postgre' | 'sqlserver' | 'oracle' | string;
    host: string;
    port: number | string;
    databaseName: string;
    schema: string;
    username: string;
    password: string;
    driverType: string;
    driverName: string;
    sid: string;
    trustCertificate: boolean;
    allowPublicKeyRetrieval: boolean;
}
export interface DatabaseConnectionTestResult {
    success: boolean;
    message: string;
}
