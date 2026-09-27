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
          v-model="config.projectPort"
          type="number"
          variant="secondary"
          size="lg"
          label="Port d'exécution"
          placeholder="ex: 8080"
          fill-width
        />
      </div>

      <div class="w-full">
        <GenesisInput 
          v-model="config.projectDescription" 
          variant="secondary" 
          label="Description du projet"
          size="lg"
          placeholder="Une brève description de l'application..."
          :type="'textarea'"
          fill-width
        />
      </div>

      <div class="w-full">
        <GenesisInput
          v-model="config.projectLocation"
          type="path"
          variant="secondary"
          label="Emplacement"
          fill-width
          size="lg"
          @request-folder-path="handleSelectFolderPath"
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
          <template #default="{ close }">
            <div class="p-1 space-y-1">
              <button
                v-for="v in availableLanguageVersions"
                :key="v"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.languageVersion === v }"
                @click="() => { updateConfig('languageVersion', v); close(); }"
              >
                {{ v }}
              </button>
            </div>
          </template>
        </GenesisInput>

        <GenesisInput
          v-model="config.buildTool"
          type="select"
          variant="secondary"
          label="Build Tool"
          placeholder="Sélectionner..."
          fill-width
        >
          <template #default="{ close }">
            <div class="p-1 space-y-1">
              <button
                v-for="tool in availableBuildTools"
                :key="tool"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.buildTool === tool }"
                @click="() => { updateConfig('buildTool', tool); close() }"
              >
                {{ tool.charAt(0).toUpperCase() + tool.slice(1) }}
              </button>
            </div>
          </template>
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
            <template #default="{ close }">
              <div class="p-1 space-y-1">
                <button
                  v-for="v in availableFrameworkVersions"
                  :key="v"
                  type="button"
                  class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                  :class="{ 'text-accent font-medium': config.frameworkVersion === v }"
                  @click="() => { updateConfig('frameworkVersion', v); close() }"
                >
                  {{ v }}
                </button>
              </div>
            </template>
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
        <div class="flex flex-row gap-4">
          <!-- ═══ GESTION DES PROFILS DE CONFIGURATION ═══ -->
          <div class="flex flex-col gap-8">
            
            <!-- Logging Level -->
            <GenesisInput
              v-model="config.loggingLevel"
              type="select"
              variant="secondary"
              label="Niveau de Logging"
              placeholder="INFO"
              fill-width
            >
              <template #default="{ close }">
                <div class="p-1 space-y-1">
                  <button
                    v-for="opt in availableLoggingLevels"
                    :key="opt"
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': config.loggingLevel === opt }"
                    @click="() => { updateConfig('loggingLevel', opt); close() }"
                  >
                    {{ opt }}
                  </button>
                </div>
              </template>
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
              <template #default="{close}">
                <div class="p-1 space-y-1">
                  <button
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': !config.securityType || config.securityType === 'NONE' }"
                    @click="() => { updateConfig('securityType', 'NONE'); close() }"
                  >
                    Aucune
                  </button>
                  <button
                    v-for="opt in availableSecurityTypes"
                    :key="opt"
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': config.securityType === opt }"
                    @click="() => { updateConfig('securityType', opt); close() }"
                  >
                    {{ opt }}
                  </button>
                </div>
              </template>
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
              <template #default="{ close }">
                <div class="p-1 space-y-1">
                  <button
                    v-for="opt in availableCacheProviders"
                    :key="opt"
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': config.cacheProvider === opt }"
                    @click="() => { updateConfig('cacheProvider', opt); close() }"
                  >
                    {{ opt }}
                  </button>
                </div>
              </template>
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
                <template #default="{ close }">
                  <div class="p-1 space-y-1">
                    <button
                      v-for="opt in availableHibernateDdlAutoOptions"
                      :key="opt"
                      type="button"
                      class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                      :class="{ 'text-accent font-medium': config.hibernateDdlAuto === opt }"
                      @click="() => { updateConfig('hibernateDdlAuto', opt); close() }"
                    >
                      {{ opt }}
                    </button>
                  </div>
                </template>
              </GenesisInput>
            </div>
          </div>

          <div class="w-fit space-y-3">
            <!-- Panneau de gestion des configurations -->
            <GenesisConfigurationPanel
              :configurations="mockConfigurations"
              :selected-config-id="selectedConfigId"
              :filtered-configs="mockFilteredConfigs"
              :search-query="configSearchQuery"
              :can-move-up="canMoveUp"
              :can-move-down="canMoveDown"
              @update:search-query="configSearchQuery = $event"
              @add="handleAddConfig"
              @delete="handleDeleteConfig"
              @rename="handleRenameConfig"
              @edit="handleEditConfig"
              @toggle-visibility="handleToggleVisibility"
              @move-up="handleMoveUp"
              @move-down="handleMoveDown"
              @select-configuration="selectedConfigId = $event"
            />
            <!-- Barre d'outils des actions -->
            <div class="flex gap-2">
              <!-- rajouter un text hover -->
              <GenesisButtonIcon :variant="'secondary'" @click="handleSaveConfig">
                <IconSave />
              </GenesisButtonIcon>
              
              <GenesisButtonIcon :variant="'secondary'" @click="handleLoadConfig" :disabled="!selectedConfigId">
                <IconDownload />
              </GenesisButtonIcon>

              <GenesisButtonIcon :variant="'secondary'" @click="handleExportConfig" :disabled="!selectedConfigId">
                <IconUpload />
              </GenesisButtonIcon>

              <GenesisButtonIcon :variant="'secondary'" @click="handleExportAllConfigs">
                <IconSave />
              </GenesisButtonIcon>

            </div>
          </div>

        </div>
      </GenesisDisclosure>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';

import IconSave from '@genesis-labs/web-core/core/components/ui/icons/IconSave.vue';
import IconDownload from '@genesis-labs/web-core/core/components/ui/icons/IconDownload.vue';
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue';

import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
// import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';


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


const configSearchQuery = ref('');
const selectedConfigId = ref<string | number | null>(null);

// Données mockées pour l'affichage (le traitement réel viendra plus tard)
const mockConfigurations = ref([
  { id: 1, name: 'Profil Par Défaut', isHidden: false, components: ['INFO', 'NONE', 'Aucun', 'none'] },
  { id: 2, name: 'Profil Production', isHidden: false, components: ['ERROR', 'JWT', 'Redis', 'validate'] }
]);

const mockFilteredConfigs = computed(() => {
  if (!configSearchQuery.value.trim()) return mockConfigurations.value;
  const query = configSearchQuery.value.toLowerCase();
  return mockConfigurations.value.filter(c => c.name.toLowerCase().includes(query));
});

const canMoveUp = computed(() => {
  if (!selectedConfigId.value) return false;
  const index = mockConfigurations.value.findIndex(c => c.id === selectedConfigId.value);
  return index > 0;
});

const canMoveDown = computed(() => {
  if (!selectedConfigId.value) return false;
  const index = mockConfigurations.value.findIndex(c => c.id === selectedConfigId.value);
  return index < mockConfigurations.value.length - 1;
});

// Handlers mockés pour l'affichage (à implémenter avec la vraie logique plus tard)
function handleSaveConfig() { console.log('[Mock] Sauvegarder la configuration actuelle'); }
function handleLoadConfig() { console.log('[Mock] Charger la configuration', selectedConfigId.value); }
function handleExportConfig() { console.log('[Mock] Exporter la configuration', selectedConfigId.value); }
function handleExportAllConfigs() { console.log('[Mock] Exporter toutes les configurations'); }
function handleAddConfig() { console.log('[Mock] Ajouter une configuration'); }
function handleDeleteConfig(id: string | number) { console.log('[Mock] Supprimer', id); }
function handleRenameConfig(id: string | number, newName: string) { console.log('[Mock] Renommer', id, newName); }
function handleEditConfig(id: string | number) { console.log('[Mock] Éditer', id); }
function handleToggleVisibility(id: string | number) { console.log('[Mock] Toggle visibilité', id); }
function handleMoveUp() { console.log('[Mock] Monter', selectedConfigId.value); }
function handleMoveDown() { console.log('[Mock] Descendre', selectedConfigId.value); }


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