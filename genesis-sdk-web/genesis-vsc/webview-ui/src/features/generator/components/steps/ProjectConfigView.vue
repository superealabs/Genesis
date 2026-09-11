<template>
    <div class="p-6 space-y-6">
        
        <!-- ═══ Section 1 : Configuration de Base ═══ -->
        <div class="space-y-4">
            <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
                Configuration du Projet
            </h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- Nom du projet -->
                <GenesisInput 
                    v-model="config.projectName" 
                    variant="secondary" 
                    label="Nom du projet" 
                    placeholder="mon-super-projet"
                    is-mandatory
                    fill-width
                />

                <!-- Description du projet (NOUVEAU) -->
                <GenesisInput 
                    v-model="config.projectDescription" 
                    variant="secondary" 
                    label="Description du projet" 
                    placeholder="Une brève description de l'application..."
                    fill-width
                />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- Localisation -->
                <GenesisInput
                    v-model="config.projectLocation"
                    variant="secondary"
                    label="Localisation"
                    :one-line="true"
                    fill-width
                    size="lg"
                >
                    <template #right>
                        <GenesisButtonIcon variant="tertiary" @click="selectFolderPath">
                            <IconFolder />
                        </GenesisButtonIcon>
                    </template>
                </GenesisInput>

                <!-- Port d'exécution (NOUVEAU) -->
                <GenesisInput
                    v-model="config.projectPort"
                    type="number"
                    variant="secondary"
                    label="Port d'exécution"
                    placeholder="ex: 8080"
                    fill-width
                />
            </div>
        </div>

        <div class="border-t border-secondary"></div>

        <!-- ═══ Section 2 : Configuration du Framework ═══ -->
        <div class="space-y-4">
            <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
                Stack Technique ({{ framework?.name || 'Non sélectionné' }})
            </h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- Version du Language (Refactorisé en GenesisInput select) -->
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

                <!-- Build Tool (Refactorisé en GenesisInput select) -->
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
                            :key="tool.value"
                            type="button"
                            class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                            :class="{ 'text-accent font-medium': config.buildTool === tool.value }"
                            @click="updateConfig('buildTool', tool.value)"
                        >
                            {{ tool.label }}
                        </button>
                    </div>
                </GenesisInput>

                <!-- Group ID (Conditionnel) -->
                <div v-if="showGroupId" class="space-y-1">
                    <GenesisInput 
                        v-model="config.groupId"
                        variant="secondary"
                        label="Group ID"
                        placeholder="com.example"
                        fill-width
                    />
                </div>

                <!-- Framework Version -->
                <div class="space-y-1">
                    <GenesisInput 
                        v-model="config.frameworkVersion"
                        variant="secondary"
                        label="Version du Framework"
                        placeholder="ex: 3.2.0"
                        fill-width
                    />
                </div>
            </div>
        </div>

        <div class="border-t border-secondary"></div>

        <!-- ═══ Section 3 : Configuration Avancée (Disclosure) ═══ -->
        <div class="space-y-4">
            <GenesisDisclosure 
                title="⚙️ Configuration Avancée du Backend" 
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
                                v-for="opt in loggingOptions"
                                :key="opt.value"
                                type="button"
                                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                                :class="{ 'text-accent font-medium': config.loggingLevel === opt.value }"
                                @click="updateConfig('loggingLevel', opt.value)"
                            >
                                {{ opt.label }}
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
                                v-for="opt in securityOptions"
                                :key="opt.value"
                                type="button"
                                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                                :class="{ 'text-accent font-medium': config.securityType === opt.value }"
                                @click="updateConfig('securityType', opt.value)"
                            >
                                {{ opt.label }}
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
                                v-for="opt in cacheOptions"
                                :key="opt.value"
                                type="button"
                                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                                :class="{ 'text-accent font-medium': config.cacheProvider === opt.value }"
                                @click="updateConfig('cacheProvider', opt.value)"
                            >
                                {{ opt.label }}
                            </button>
                        </div>
                    </GenesisInput>

                </div>
            </GenesisDisclosure>
        </div>

    </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useGenerator } from '../../composables/useGenerator';
import { MOCK_BUILD_TOOLS, MOCK_JAVA_VERSIONS, MOCK_NODE_VERSIONS } from '../../types/generator.types';

// Imports des composants
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';
import GenesisDisclosure from '@/core/components/layouts/GenesisDisclosure.vue';
import IconFolder from '@/core/components/ui/icons/IconFolder.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import { generatorService } from '../../services/generator.service';

// ✅ Ajout de updateConfig pour gérer les clics dans les selects custom
const { stepperData, selectFolderPath, updateConfig, initializeService } = useGenerator(generatorService);

onMounted(() => {
    initializeService(); // Initialise les écouteurs postMessage une seule fois
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

// ✅ Options pour les dropdowns avancés
const loggingOptions = [
    { label: 'DEBUG', value: 'DEBUG' },
    { label: 'INFO', value: 'INFO' },
    { label: 'WARN', value: 'WARN' },
    { label: 'ERROR', value: 'ERROR' }
];

const securityOptions = [
    { label: 'Aucune', value: 'none' },
    { label: 'JWT (JSON Web Token)', value: 'jwt' },
    { label: 'Session Cookie', value: 'session' },
    { label: 'OAuth2', value: 'oauth2' }
];

const cacheOptions = [
    { label: 'Aucun', value: 'none' },
    { label: 'Redis', value: 'redis' },
    { label: 'Ehcache', value: 'ehcache' },
    { label: 'Caffeine', value: 'caffeine' }
];
</script>