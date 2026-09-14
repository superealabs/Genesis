<template>
    <div class="flex flex-col gap-4 p-4 max-w-3xl mx-auto">
        <h3 class="text-lg font-semibold text-text mb-2">Configuration de la Connexion</h3>

        <!-- Moteur de Base de Données (Lecture seule, choisi à l'étape précédente) -->
        <div class="flex flex-col gap-1">
            <label class="text-sm font-medium text-muted">
                SGBD Sélectionné <span class="text-accent ml-0.5">*</span>
            </label>
            <GenesisInput
                :model-value="database.engine"
                disabled
                fill-width
                class="capitalize"
            />
        </div>

        <!-- Hôte et Port -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <GenesisInput
                v-model="database.host"
                label="Host IP"
                placeholder="127.0.0.1"
                is-mandatory
                fill-width
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

        <!-- URL (Calculée) et Bouton de Test -->
        <div class="flex flex-col md:flex-row items-end gap-2">
            <GenesisInput
                :model-value="computedUrl"
                label="URL (auto-complétée)"
                placeholder="jdbc:postgresql://localhost:5432/my_database"
                disabled
                fill-width
                class="flex-1"
            />
            <GenesisButton
                :variant="'secondary'" 
                content-align="center"
                :disabled="isTesting"
                @click="handleTestConnection"
                class="w-full md:w-auto"
            >
                <span v-if="isTesting">Test en cours...</span>
                <span v-else>Test Connexion</span>
            </GenesisButton>
        </div>

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
import { computed, ref } from 'vue';
import { useGenerator } from '../../composables/useGenerator';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';

// ✅ CORRECTION : Utiliser le store au lieu d'un ref local
const { stepperData, updateDatabase, testDatabaseConnection } = useGenerator();
const database = computed(() => stepperData.value.database);

const emit = defineEmits<{
    'test-connection-error': [message: string];
}>();

// ✅ NOUVEAU : État de chargement local pour le bouton
const isTesting = ref(false);

// ✅ URL calculée dynamiquement en fonction du moteur choisi à l'étape précédente
const computedUrl = computed(() => {
    const { engine, host, port, databaseName, sid } = database.value;
    const hostStr = host || 'localhost';
    const portStr = port || '';
    const dbName = databaseName || '';
    
    switch (engine) {
        case 'mysql': 
            return `jdbc:mysql://${hostStr}:${portStr}/${dbName}`;
        case 'postgre': 
            return `jdbc:postgresql://${hostStr}:${portStr}/${dbName}`;
        case 'sqlserver': 
            return `jdbc:sqlserver://${hostStr}:${portStr};databaseName=${dbName}`;
        case 'oracle': 
            return `jdbc:oracle:thin:@${hostStr}:${portStr}:${sid || dbName}`;
        default: 
            return '';
    }
});

// ✅ Gestionnaire du test de connexion
async function handleTestConnection() {
    isTesting.value = true;
    try {
        const result = await testDatabaseConnection();
        
        if (!result.success) {
            // On remonte l'erreur au parent (GeneratorStepper) pour qu'il affiche l'ErrorPopup
            emit('test-connection-error', result.message);
        }
        // Si success === true, l'utilisateur voit juste que le chargement s'arrête. 
        // (Tu pourras ajouter un toast de succès ici plus tard si besoin)
        
    } catch (error) {
        // Fallback pour les erreurs inattendues (ex: problème réseau)
        const msg = error instanceof Error ? error.message : 'Une erreur inconnue est survenue lors du test.';
        emit('test-connection-error', msg);
    } finally {
        isTesting.value = false;
    }
}
</script>