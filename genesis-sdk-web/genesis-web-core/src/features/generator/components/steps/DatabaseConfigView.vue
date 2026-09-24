<template>
  <div class="flex flex-col gap-4 p-4 max-w-3xl mx-auto">
    <h3 class="text-lg font-semibold text-text mb-2">Configuration de la Connexion</h3>

    <!-- 1. MOTEUR (Lecture seule) -->
    <div class="flex flex-col gap-1">
      <label class="text-sm font-medium text-muted">
        SGBD Sélectionné <span class="text-accent ml-0.5">*</span>
      </label>
      <GenesisInput
        :model-value="database.engine"
        label="Moteur"
        disabled
        fill-width
        class="capitalize"
      />
    </div>

    <!-- 2. HÔTE ET PORT -->
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

    <!-- 3. BASE DE DONNÉES ET SCHÉMA -->
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

    <!-- 4. IDENTIFIANTS -->
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

    <!-- 5. URL CALCULÉE ET TEST DE CONNEXION -->
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
        variant="secondary" 
        content-align="center"
        :disabled="isTesting"
        @click="handleTestConnection"
        class="w-full md:w-auto"
      >
        <span v-if="isTesting">Test en cours...</span>
        <span v-else>Test Connexion</span>
      </GenesisButton>
    </div>

    <!-- 6. CONFIGURATIONS AVANCÉES -->
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

    <!-- 7. OPTIONS BOOLÉENNES -->
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
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';

// ============================================================================
// 1. EMITS
// ============================================================================
const emit = defineEmits<{
  'test-connection-error': [message: string];
}>();

// ============================================================================
// 2. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const { stepperData, testDatabaseConnection } = useGenerator();

// Référence réactive vers la configuration de la base de données dans le store
const database = computed(() => stepperData.value.database);

// État local pour gérer le chargement du bouton de test
const isTesting = ref(false);

// ============================================================================
// 3. COMPUTEDS (LOGIQUE D'AFFICHAGE)
// ============================================================================

/**
 * Construit dynamiquement l'URL de connexion JDBC en fonction du moteur sélectionné.
 * Cette logique centralise le formatage des chaînes de connexion pour éviter 
 * les erreurs de saisie manuelle de l'utilisateur et garantir la compatibilité JDBC.
 */
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

// ============================================================================
// 4. ACTIONS
// ============================================================================

/**
 * Déclenche le test de connexion via le service et gère le retour.
 * En cas d'échec, l'erreur est émise vers le composant parent (GeneratorStepper) 
 * qui se charge de l'afficher via l'ErrorPopup global. Cela garde ce composant 
 * "dumb" et responsable uniquement de la collecte des données et du déclenchement de l'action.
 */
async function handleTestConnection() {
  isTesting.value = true;
  
  try {
    const result = await testDatabaseConnection();
    
    if (!result.success) {
      emit('test-connection-error', result.message);
    }
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : 'Une erreur inconnue est survenue lors du test.';
    emit('test-connection-error', errorMessage);
  } finally {
    isTesting.value = false;
  }
}
</script>