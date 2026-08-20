export type Language = 'Java' | 'Python' | 'Node.js' | 'PHP';
export type Framework = 'Spring Boot' | 'Django' | 'Express' | 'Laravel';
export type Database = 'PostgreSQL' | 'MySQL' | 'MongoDB' | 'Oracle';
export type Frontend = 'Vue' | 'React' | 'Angular' | 'None';

export type DatabaseConfig = {
    host: string;
    port: string;
    name: string;
    username: string;
    password: string;
}

export type GeneratorForm = {
    language: Language | null;
    framework: Framework | null;
    database: Database | null;
    databaseConfig: DatabaseConfig;
    frontend: Frontend | null;
}