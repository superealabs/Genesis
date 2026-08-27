<template>
    <div class="flex flex-col gap-4 p-4 max-w-3xl mx-auto">
        <h3 class="text-lg font-semibold text-text mb-2">Configuration de la Base de Données</h3>

        <!-- Moteur de Base de Données -->
        <div class="flex flex-col gap-1">
            <label class="text-sm font-medium text-muted">
                SGBD <span class="text-accent ml-0.5">*</span>
            </label>
            <GenesisDropdown :match-trigger-width="true" trigger-variant="secondary" fill-width>
                <template #trigger>
                    <span>{{ selectedDatabaseLabel }}</span>
                </template>
                <div class="py-1">
                    <button
                        v-for="db in databases"
                        :key="db.value"
                        type="button"
                        class="w-full text-left px-3 py-1.5 text-sm text-text hover:bg-[var(--color-hover-ghost)] transition-colors flex items-center justify-between"
                        @click="selectDatabase(db.value)"
                    >
                        <span>{{ db.label }}</span>
                        <span v-if="database.engine === db.value" class="text-accent">✓</span>
                    </button>
                </div>
            </GenesisDropdown>
        </div>

        <!-- Hôte et Port -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <GenesisInput
                v-model="database.host"
                label="Host IP"
                placeholder="127.0.0.1"
                is-mandatory
                fill-width
                class="md:col-span-2"
            />
            <GenesisInput
                v-model.number="database.port"
                label="Port"
                type="number"
                placeholder="5432"
                is-mandatory
                fill-width
            />
        </div>

        <!-- Base de données et Schéma -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <GenesisInput
                v-model="database.databaseName"
                label="Database Name"
                placeholder="my_database"
                is-mandatory
                fill-width
            />
            <GenesisInput
                v-model="database.schema"
                label="Schema"
                placeholder="public"
                fill-width
            />
        </div>

        <!-- Identifiants -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <GenesisInput
                v-model="database.username"
                label="Username"
                placeholder="root"
                is-mandatory
                fill-width
            />
            <GenesisInput
                v-model="database.password"
                label="Password"
                type="password"
                placeholder="••••••••"
                fill-width
            />
        </div>

        <!-- URL (Calculée) -->
        <GenesisInput
            :model-value="computedUrl"
            label="URL (auto-complétée)"
            placeholder="jdbc:postgresql://localhost:5432/my_database"
            disabled
            fill-width
        />

        <!-- Configurations avancées -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <GenesisInput
                v-model="database.driverType"
                label="Driver Type"
                placeholder="org.postgresql.Driver"
                fill-width
            />
            <GenesisInput
                v-model="database.driverName"
                label="Driver Name"
                placeholder="PostgreSQL JDBC Driver"
                fill-width
            />
            <GenesisInput
                v-model="database.sid"
                label="SID (Oracle)"
                placeholder="ORCL"
                :disabled="database.engine !== 'oracle'"
                fill-width
            />
        </div>

        <!-- Options Booléennes -->
        <div class="flex flex-col gap-2 mt-2">
            <!-- Note: pour les booléens, il vaut mieux utiliser type="checkbox" si ton GenesisInput le supporte, ou un composant GenesisCheckbox dédié -->
            <GenesisInput
                v-model="database.trustCertificate"
                label="Trust certificate"
                type="boolean"
                one-line
            />
            <GenesisInput
                v-model="database.allowPublicKeyRetrieval"
                label="Allow public key retrieval"
                type="boolean"
                one-line
            />
        </div>


    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '../composables/useGenerator';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue'; // Assure-toi qu'il existe

type DatabaseEngine = 'mysql' | 'postgre' | 'sqlserver' | 'oracle';

const databases: { label: string; value: DatabaseEngine; defaultPort: number }[] = [
    { label: 'PostgreSQL', value: 'postgre', defaultPort: 5432 },
    { label: 'MySQL', value: 'mysql', defaultPort: 3306 },
    { label: 'SQL Server', value: 'sqlserver', defaultPort: 1433 },
    { label: 'Oracle', value: 'oracle', defaultPort: 1521 },
];

// ✅ CORRECTION : Utiliser le store au lieu d'un ref local
const { stepperData, updateDatabase } = useGenerator();
const database = computed(() => stepperData.value.database);

const selectedDatabaseLabel = computed(() => {
    return databases.find(db => db.value === database.value.engine)?.label || 'Sélectionner une base de données';
});

function selectDatabase(engine: DatabaseEngine) {
    updateDatabase('engine', engine);
    const db = databases.find(d => d.value === engine);
    if (db) {
        updateDatabase('port', db.defaultPort);
    }
}

const computedUrl = computed(() => {
    const { engine, host, port, databaseName, sid } = database.value;
    const hostStr = host || 'localhost';
    
    switch (engine) {
        case 'mysql': return `jdbc:mysql://${hostStr}:${port}/${databaseName}`;
        case 'postgre': return `jdbc:postgresql://${hostStr}:${port}/${databaseName}`;
        case 'sqlserver': return `jdbc:sqlserver://${hostStr}:${port};databaseName=${databaseName}`;
        case 'oracle': return `jdbc:oracle:thin:@${hostStr}:${port}:${sid || databaseName}`;
        default: return '';
    }
});
</script>