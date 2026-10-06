<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : FORMULAIRE DE CONFIGURATION -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      <h3 class="text-lg font-semibold text-text mb-2">Configuration de la Connexion</h3>

      <!-- 1. MOTEUR (Sélectionnable avec le même pattern que le Framework) -->
      <div ref="databaseSelectorRef" class="flex items-end gap-3 pb-4 border-b border-neutral-light">
        <div class="flex-1">
          <template v-if="isEditingDatabase">
            <GenesisInput
              type="combobox"
              :options="availableDatabasesOptions"
              :model-value="tempDatabaseName"
              @update:model-value="handleDatabaseSelect"
              label="SGBD"
              placeholder="Rechercher un SGBD..."
              size="lg"
              fill-width
            />
          </template>
          <template v-else>
            <div class="flex flex-col gap-1">
              <label class="text-sm font-medium text-muted">
                SGBD Sélectionné <span class="text-accent ml-0.5">*</span>
              </label>
              <div class="flex items-center gap-2 h-10 px-3 bg-bg-secondary/30 rounded text-text border border-transparent">
                <span class="truncate font-medium capitalize">{{ database.engine || 'Aucun SGBD sélectionné' }}</span>
              </div>
            </div>
          </template>
        </div>
        
        <div class="pb-0.5">
          <GenesisButton v-if="!isEditingDatabase" variant="secondary" size="lg" @click="startEditingDatabase">
            Modifier
          </GenesisButton>
          <GenesisButton v-else variant="tertiary" size="lg" @click="cancelEditingDatabase">
            Annuler
          </GenesisButton>
        </div>
      </div>

      <!-- 2. HÔTE ET PORT -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <GenesisInput
          v-model="database.host"
          label="Host IP"
          size="lg"
          placeholder="127.0.0.1"
          is-mandatory
          fill-width
        />
        <GenesisInput
          v-model.number="database.port"
          label="Port"
          type="number"
          size="lg"
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
          size="lg"
          placeholder="my_database"
          is-mandatory
          fill-width
        />
        <GenesisInput
          v-model="database.schema"
          label="Schema"
          size="lg"
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
          size="lg"
          is-mandatory
          fill-width
        />
        <GenesisInput
          v-model="database.password"
          label="Password"
          type="password"
          size="lg"
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
          size="lg"
          fill-width
          class="flex-1"
        />
        <GenesisButton
          variant="secondary" 
          content-align="center"
          :disabled="isTesting"
          size="lg"
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
          size="lg"
          placeholder="org.postgresql.Driver"
          fill-width
        />
        <GenesisInput
          v-model="database.driverName"
          label="Driver Name"
          placeholder="PostgreSQL JDBC Driver"
          size="lg"
          fill-width
        />
        <GenesisInput
          v-model="database.sid"
          label="SID (Oracle)"
          placeholder="ORCL"
          size="lg"
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
          size="lg"
          one-line
        />
        <GenesisInput
          v-model="database.allowPublicKeyRetrieval"
          label="Allow public key retrieval"
          type="boolean"
          size="lg"
          one-line
        />
      </div>
    </div>

    <!-- PARTIE DROITE : CARROUSEL TUTORIEL -->
    <div class="w-1/2 h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useDatabaseStore } from '@genesis-labs/web-core/features/database/store/useDatabase.store'; // ✅ NOUVEAU IMPORT

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

// ============================================================================
// 1. EMITS
// ============================================================================
const emit = defineEmits<{
  'test-connection-error': [message: string];
}>();

// ============================================================================
// 2. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const { stepperData, testDatabaseConnection, setDatabaseEngine } = useGenerator();
const databaseStore = useDatabaseStore();

// Référence réactive vers la configuration de la base de données dans le store
const database = computed(() => stepperData.value.database);

// État local pour gérer le chargement du bouton de test
const isTesting = ref(false);

// ============================================================================
// 3. GESTION DE L'ÉDITION DU SGBD (Pattern identique au Framework)
// ============================================================================
const databaseSelectorRef = ref<HTMLElement | null>(null);
const isEditingDatabase = ref(false);
const tempDatabaseName = ref('');

const availableDatabasesOptions = computed(() => 
  databaseStore.availableEngines.map(e => ({ label: e.name, value: e }))
);

function startEditingDatabase() {
  tempDatabaseName.value = '';
  isEditingDatabase.value = true;
}

function cancelEditingDatabase() {
  isEditingDatabase.value = false;
  tempDatabaseName.value = '';
}

function handleDatabaseSelect(value: any) {
  // Si l'utilisateur tape du texte sans sélectionner d'option, on ignore
  if (typeof value === 'string') return;
  
  // Si une option valide (objet DatabaseEngine) est sélectionnée
  if (value && value.name) {
    setDatabaseEngine(value);
    isEditingDatabase.value = false;
  }
}

function handleClickOutsideDb(event: MouseEvent) {
  if (isEditingDatabase.value && databaseSelectorRef.value && !databaseSelectorRef.value.contains(event.target as Node)) {
    cancelEditingDatabase();
  }
}

watch(isEditingDatabase, (isEditing) => {
  if (isEditing) {
    setTimeout(() => window.addEventListener('mousedown', handleClickOutsideDb), 0);
  } else {
    window.removeEventListener('mousedown', handleClickOutsideDb);
  }
});

// ============================================================================
// 4. CONSTANTES (CARROUSEL)
// ============================================================================
const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Hôte et Port' },
  { color: '#EF4444', label: 'Étape 2 : Base de données' },
  { color: '#10B981', label: 'Étape 3 : Test de connexion' },
];

// ============================================================================
// 5. COMPUTEDS (LOGIQUE D'AFFICHAGE)
// ============================================================================

/**
 * Construit dynamiquement l'URL de connexion JDBC en fonction du moteur sélectionné.
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
// 6. ACTIONS
// ============================================================================

/**
 * Déclenche le test de connexion via le service et gère le retour.
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