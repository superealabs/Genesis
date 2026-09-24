<template>
  <div class="p-6 space-y-6">
    
    <!-- 1. CONFIGURATION DE BASE -->
    <div class="space-y-4">
      <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
        Configuration du Projet
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <GenesisInput 
          v-model="config.projectName" 
          variant="secondary" 
          label="Nom du projet" 
          placeholder="mon-super-projet"
          size="lg"
          is-mandatory
          fill-width
        />
        <GenesisInput 
          v-model="config.projectDescription" 
          variant="secondary" 
          label="Description du projet"
          size="lg"
          placeholder="Une brève description de l'application..."
          fill-width
        />
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <GenesisInput
          v-model="config.projectLocation"
          type="path"
          variant="secondary"
          label="Localisation"
          fill-width
          size="lg"
          @request-folder-path="handleSelectFolderPath"
        />
        <GenesisInput
          v-model="config.projectPort"
          type="number"
          variant="secondary"
          size="lg"
          label="Port d'exécution"
          placeholder="ex: 8080"
          fill-width
        />
      </div>
    </div>

    <div class="border-t border-secondary"></div>

    <!-- 2. CONFIGURATION DU FRAMEWORK -->
    <div class="space-y-4">
      <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
        Stack Technique ({{ framework?.name || 'Non sélectionné' }})
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <GenesisInput
          v-model="config.languageVersion"
          type="select"
          variant="secondary"
          label="Version du Language"
          placeholder="Sélectionner..."
          fill-width
        >
          <div class="p-1 space-y-1">
            <button
              v-for="v in availableLanguageVersions"
              :key="v"
              type="button"
              class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
              :class="{ 'text-accent font-medium': config.languageVersion === v }"
              @click="updateConfig('languageVersion', v)"
            >
              {{ v }}
            </button>
          </div>
        </GenesisInput>

        <GenesisInput
          v-model="config.buildTool"
          type="select"
          variant="secondary"
          label="Build Tool"
          placeholder="Sélectionner..."
          fill-width
        >
          <div class="p-1 space-y-1">
            <button
              v-for="tool in availableBuildTools"
              :key="tool"
              type="button"
              class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
              :class="{ 'text-accent font-medium': config.buildTool === tool }"
              @click="updateConfig('buildTool', tool)"
            >
              {{ tool.charAt(0).toUpperCase() + tool.slice(1) }}
            </button>
          </div>
        </GenesisInput>

        <div v-if="showGroupId" class="space-y-1">
          <GenesisInput 
            v-model="config.groupId"
            variant="secondary"
            label="Group ID"
            placeholder="com.example"
            fill-width
          />
        </div>

        <div class="space-y-1">
          <GenesisInput 
            v-model="config.frameworkVersion"
            type="select"
            variant="secondary"
            label="Version du Framework"
            placeholder="Sélectionner..."
            fill-width
          >
            <div class="p-1 space-y-1">
              <button
                v-for="v in availableFrameworkVersions"
                :key="v"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.frameworkVersion === v }"
                @click="updateConfig('frameworkVersion', v)"
              >
                {{ v }}
              </button>
            </div>
          </GenesisInput>
        </div>
      </div>
    </div>

    <div class="border-t border-secondary"></div>

    <!-- 3. CONFIGURATION AVANCÉE -->
    <div class="space-y-4">
      <GenesisDisclosure 
        title="Configuration Avancée du Backend" 
        :default-open="false"
        variant="secondary"
      >
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
          
          <!-- Logging Level -->
          <GenesisInput
            v-model="config.loggingLevel"
            type="select"
            variant="secondary"
            label="Niveau de Logging"
            placeholder="INFO"
            fill-width
          >
            <div class="p-1 space-y-1">
              <button
                v-for="opt in availableLoggingLevels"
                :key="opt"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.loggingLevel === opt }"
                @click="updateConfig('loggingLevel', opt)"
              >
                {{ opt }}
              </button>
            </div>
          </GenesisInput>

          <!-- Security Type -->
          <GenesisInput
            v-model="config.securityType"
            type="select"
            variant="secondary"
            label="Type de Sécurité"
            placeholder="Aucune"
            fill-width
          >
            <div class="p-1 space-y-1">
              <button
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': !config.securityType || config.securityType === 'NONE' }"
                @click="updateConfig('securityType', 'NONE')"
              >
                Aucune
              </button>
              <button
                v-for="opt in availableSecurityTypes"
                :key="opt"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.securityType === opt }"
                @click="updateConfig('securityType', opt)"
              >
                {{ opt }}
              </button>
            </div>
          </GenesisInput>

          <!-- Cache Provider -->
          <GenesisInput
            v-model="config.cacheProvider"
            type="select"
            variant="secondary"
            label="Fournisseur de Cache"
            placeholder="Aucun"
            fill-width
          >
            <div class="p-1 space-y-1">
              <button
                v-for="opt in availableCacheProviders"
                :key="opt"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.cacheProvider === opt }"
                @click="updateConfig('cacheProvider', opt)"
              >
                {{ opt }}
              </button>
            </div>
          </GenesisInput>

          <!-- Hibernate DDL Auto -->
          <div v-if="showHibernateDdl" class="space-y-1">
            <GenesisInput
              v-model="config.hibernateDdlAuto"
              type="select"
              variant="secondary"
              label="Hibernate DDL Auto"
              placeholder="none"
              fill-width
            >
              <div class="p-1 space-y-1">
                <button
                  v-for="opt in availableHibernateDdlAutoOptions"
                  :key="opt"
                  type="button"
                  class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                  :class="{ 'text-accent font-medium': config.hibernateDdlAuto === opt }"
                  @click="updateConfig('hibernateDdlAuto', opt)"
                >
                  {{ opt }}
                </button>
              </div>
            </GenesisInput>
          </div>

        </div>
      </GenesisDisclosure>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';

// ============================================================================
// 1. EMITS
// ============================================================================
const emit = defineEmits<{
  'request-folder-path': [];
}>();

// ============================================================================
// 2. COMPOSABLES
// ============================================================================
const { 
  stepperData,
  updateConfig,
  availableLoggingLevels,
  availableSecurityTypes,
  availableCacheProviders,
  availableLanguageVersions,    
  availableFrameworkVersions,   
  availableBuildTools,
  availableHibernateDdlAutoOptions,
  fetchLoggingLevels, 
  fetchSecurityTypes,
  fetchCacheProviders,
  fetchLanguageVersions,        
  fetchFrameworkVersions,
  fetchBuildTools,
  fetchHibernateDdlAutoOptions
} = useGenerator();

// ============================================================================
// 3. COMPUTEDS (Données dérivées)
// ============================================================================
const config = computed(() => stepperData.value.config);
const framework = computed(() => stepperData.value.framework);

const showGroupId = computed(() => framework.value?.withGroupId === true);
const showHibernateDdl = computed(() => framework.value?.withHibernateDdlAuto === true);

// ============================================================================
// 4. ACTIONS
// ============================================================================
function handleSelectFolderPath() {
  emit('request-folder-path');
}

// ============================================================================
// 5. WATCHERS (Logique réactive)
// ============================================================================

/**
 * Charge les options de configuration spécifiques au framework sélectionné.
 * L'option { immediate: true } permet d'exécuter ce watcher dès le montage 
 * du composant, évitant ainsi la duplication de code avec un hook onMounted.
 * L'utilisation de Promise.all optimise le temps de chargement en exécutant 
 * les requêtes API en parallèle plutôt qu'en séquence.
 */
watch(
  () => framework.value?.id,
  async (newId) => {
    if (newId && framework.value) {
      await Promise.all([
        fetchBuildTools(newId),
        fetchLanguageVersions(framework.value.languageId),
        fetchFrameworkVersions(newId),
        fetchLoggingLevels(newId),
        fetchSecurityTypes(newId),
        fetchCacheProviders(newId),
        fetchHibernateDdlAutoOptions(newId)
      ]);
    }
  },
  { immediate: true }
);
</script>