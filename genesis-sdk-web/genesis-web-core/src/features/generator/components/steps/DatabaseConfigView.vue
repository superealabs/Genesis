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
                <span class="truncate font-medium capitalize">{{ selectedEngineName }}</span>
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
        
        <!-- MODIFIÉ : Prend 2 colonnes sur écran moyen+ si le moteur n'est ni Postgres ni SQL Server -->
        <div :class="database.engine == 'mysql' ? 'md:col-span-2' : 'md:col-span-1'">
          <GenesisInput
            v-model="database.databaseName"
            label="Database Name"
            size="lg"
            placeholder="my_database"
            is-mandatory
            fill-width
          />
        </div>

        <div v-if="database.engine === 'oracle'" class="md:col-span-1">
          <GenesisInput
            v-model="database.sid"
            label="SID (Oracle)"
            placeholder="ORCL"
            size="lg"
            fill-width
          />
        </div>
        <!-- MODIFIÉ : Visible uniquement pour PostgreSQL et SQL Server (occupe 1 colonne) -->
        <div v-if="database.engine === 'postgre' || database.engine === 'sqlserver'" class="md:col-span-1">
          <GenesisInput
            v-model="database.schema"
            label="Schema"
            size="lg"
            :placeholder="database.engine === 'postgre' ? 'public' : 'dbo'"
            fill-width
          />
        </div>
        
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

      <!-- 6. CONFIGURATIONS AVANCÉES (Drivers & Spécifique Oracle) -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- ✅ MODIFIÉ : Désactivé car géré automatiquement par le store lors de la sélection du SGBD -->
        <GenesisInput
          v-model="database.driverType"
          label="Driver Type"
          size="lg"
          placeholder="org.postgresql.Driver"
          disabled
          fill-width
        />
        <GenesisInput
          v-model="database.driverName"
          label="Driver Name"
          placeholder="PostgreSQL JDBC Driver"
          size="lg"
          disabled
          fill-width
        />
      
      </div>

      <!-- 7. OPTIONS BOOLÉENNES SPÉCIFIQUES -->
      <div class="flex flex-col gap-2 mt-2">
        <!-- ✅ MODIFIÉ : Visible uniquement pour MySQL -->
        <GenesisInput
          v-if="database.engine === 'mysql'"
          v-model="database.allowPublicKeyRetrieval"
          label="Allow public key retrieval"
          type="boolean"
          size="lg"
          one-line
        />
        
        <GenesisInput
          v-model="database.trustCertificate"
          label="Trust certificate"
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
import { useDatabaseStore } from '@genesis-labs/web-core/features/database/store/useDatabase.store';

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
const { 
  stepperData, 
  pendingDatabaseEngine,      
  setPendingDatabaseEngine,   
  testDatabaseConnection 
} = useGenerator();
const databaseStore = useDatabaseStore();

const database = computed(() => stepperData.value.database);
const isTesting = ref(false);
const selectedEngineName = computed(() => 
  pendingDatabaseEngine.value?.name || stepperData.value.database.engine || 'Aucun SGBD sélectionné'
);

const dbConfig = computed(() => stepperData.value.database);

// ============================================================================
// 3. GESTION DE L'ÉDITION DU SGBD
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
  if (typeof value === 'string') return;
  if (value && value.name) {
    setPendingDatabaseEngine(value);
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
// 5. COMPUTEDS
// ============================================================================
const computedUrl = computed(() => {
  const { engine, host, port, databaseName, sid } = dbConfig.value;
  const hostStr = host || 'localhost';
  const portStr = port || '';
  const dbName = databaseName || '';
  
  switch (engine) {
    case 'mysql': return `jdbc:mysql://${hostStr}:${portStr}/${dbName}`;
    case 'postgre': return `jdbc:postgresql://${hostStr}:${portStr}/${dbName}`;
    case 'sqlserver': return `jdbc:sqlserver://${hostStr}:${portStr};databaseName=${dbName}`;
    case 'oracle': return `jdbc:oracle:thin:@${hostStr}:${portStr}:${sid || dbName}`;
    default: return '';
  }
});

watch(
  () => [database.value.host, database.value.port, database.value.databaseName, database.value.sid],
  () => {
    // Le computed se mettra à jour automatiquement grâce à la réactivité de Vue
  },
  { deep: true }
);

// ============================================================================
// 6. ACTIONS
// ============================================================================
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