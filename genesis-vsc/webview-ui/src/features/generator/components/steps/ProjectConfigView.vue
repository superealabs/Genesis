<template>
    <div class="p-6 space-y-6">
        <!-- Section: Configuration du Projet -->
        <div class="space-y-4">
            <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
                Configuration du Projet
            </h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <GenesisInput v-model="config.projectName" :variant="'secondary'" label="Nom du projet" placeholder="nom du projet"/>


                <!-- L'input gère maintenant le layout interne grâce au slot rightIcon -->
                <GenesisInput
                    v-model="config.projectLocation"
                    placeholder="/chemin/vers/le/projet"
                    :variant="'secondary'"
                    label="Localisation"
                    :one-line="true"
                    :fill-width="true"
                    :size="'lg'"
                >
                    <template #right>
                        <GenesisButtonIcon :variant="'tertiary'" @click="selectFolderPath">
                            <IconFolder />
                        </GenesisButtonIcon>
                    </template>
                </GenesisInput>
            </div>
        </div>

        <!-- Section: Configuration du Framework (Selects inchangés pour l'instant) -->
        <div class="space-y-4">
            <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
                Configuration du Framework ({{ framework?.name || 'Non sélectionné' }})
            </h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- Version du Language -->
                <div class="space-y-1">
                    <label class="text-sm font-medium text-text-muted">Version du Language</label>
                    <select 
                        v-model="config.languageVersion"
                        class="w-full px-3 py-2 bg-bg-light border border-secondary rounded-md focus:outline-none focus:ring-2 focus:ring-accent/50 text-text"
                    >
                        <option value="">Sélectionner...</option>
                        <option v-for="v in availableLanguageVersions" :key="v" :value="v">{{ v }}</option>
                    </select>
                </div>

                <!-- Build Tool -->
                <div class="space-y-1">
                    <label class="text-sm font-medium text-text-muted">Build Tool</label>
                    <select 
                        v-model="config.buildTool"
                        class="w-full px-3 py-2 bg-bg-light border border-secondary rounded-md focus:outline-none focus:ring-2 focus:ring-accent/50 text-text"
                    >
                        <option v-for="tool in availableBuildTools" :key="tool.value" :value="tool.value">
                            {{ tool.label }}
                        </option>
                    </select>
                </div>

                <!-- Group ID (Conditionnel) -->
                <div v-if="showGroupId" class="space-y-1">
                    <label class="text-sm font-medium text-text-muted">Group ID</label>
                    <GenesisInput 
                        v-model="config.groupId"
                        placeholder="com.example"
                    />
                </div>

                <!-- Framework Version -->
                <div class="space-y-1">
                    <label class="text-sm font-medium text-text-muted">Version du Framework</label>
                    <GenesisInput 
                        v-model="config.frameworkVersion"
                        placeholder="ex: 3.2.0"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useGenerator } from '../../composables/useGenerator';
import { generatorService } from '../../services/generator.service';
import { MOCK_BUILD_TOOLS, MOCK_JAVA_VERSIONS, MOCK_NODE_VERSIONS } from '../../types/generator.types';

// Imports des composants
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';

import IconFolder from '@/core/components/ui/icons/IconFolder.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';

const { stepperData, selectFolderPath } = useGenerator();

onMounted(() => {
    generatorService.init();
});

const config = computed(() => stepperData.value.config);
const framework = computed(() => stepperData.value.framework);

const showGroupId = computed(() => {
    return framework.value?.coreFramework === 'Spring' || framework.value?.coreFramework === 'Laravel';
});

const availableLanguageVersions = computed(() => {
    const core = framework.value?.coreFramework;
    if (core === 'Spring' || core === 'Laravel') return MOCK_JAVA_VERSIONS;
    if (core === 'Express' || core === 'Django') return MOCK_NODE_VERSIONS;
    return ['Latest'];
});

const availableBuildTools = computed(() => MOCK_BUILD_TOOLS);
</script>