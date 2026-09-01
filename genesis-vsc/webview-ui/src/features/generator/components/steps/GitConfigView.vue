<template>
    <div class="flex flex-col gap-6 p-4 max-w-3xl mx-auto">
        
        <!-- ═══ Section 1 : Activation Git ═══ -->
        <div class="space-y-3">
            <h3 class="text-lg font-semibold text-text flex items-center gap-2">
                Configuration Git
            </h3>
            <p class="text-sm text-text-muted">
                Configurez l'initialisation du dépôt Git pour votre projet.
            </p>

            <div class="flex flex-col gap-2">
                <GenesisInput
                    v-model="gitConfig.useGit"
                    type="boolean"
                    label="Initialiser un dépôt Git"
                    one-line
                />

                <GenesisInput
                    v-if="gitConfig.useGit"
                    v-model="gitConfig.useRemoteRepo"
                    type="boolean"
                    label="Utiliser un dépôt distant (Remote)"
                    one-line
                />
            </div>
        </div>

        <!-- ═══ Section 2 : Configuration Remote (si useRemoteRepo) ═══ -->
        <div v-if="gitConfig.useGit && gitConfig.useRemoteRepo" class="space-y-4 p-4 bg-bg-light/50 rounded-lg border border-secondary">
            
            <!-- Choix : Nouveau repo ou existant -->
            <GenesisInput
                v-model="gitConfig.isNewRemoteRepo"
                type="boolean"
                label="Créer un nouveau dépôt distant"
                one-line
                class="mb-2"
            />

            <!-- Nom d'utilisateur (toujours requis pour remote) -->
            <GenesisInput
                v-model="gitConfig.githubUsername"
                type="text"
                label="Nom d'utilisateur GitHub"
                placeholder="votre-username"
                is-mandatory
                fill-width
            />

            <div v-if="gitConfig.isNewRemoteRepo" class="space-y-3">
                <GenesisInput
                    v-model="gitConfig.githubToken"
                    type="password"
                    label="Personal Access Token (GitHub)"
                    placeholder="ghp_..."
                    is-mandatory
                    fill-width
                />
                <p class="text-xs text-text-muted">
                    ⚠️ Le token doit avoir les permissions <code class="bg-secondary px-1 rounded">repo</code>.
                </p>
            </div>

            <div class="border-t border-secondary my-4"></div>

            <!-- Choix Mono-repo vs Multi-repo -->
            <GenesisInput
                v-model="gitConfig.separateRepositories"
                type="boolean"
                label="Séparer les dépôts (Frontend & Backend)"
                one-line
                class="mb-2"
            />

            <!-- Cas 1 : Mono-repo -->
            <div v-if="!gitConfig.separateRepositories" class="flex flex-col gap-2">
                <GenesisInput
                    v-model="gitConfig.repositoryName"
                    type="text"
                    label="Nom du dépôt"
                    placeholder="mon-projet-complet"
                    is-mandatory
                    fill-width
                />
            </div>

            <!-- Cas 2 : Deux dépôts séparés -->
            <div v-else class="flex flex-col gap-2">
                <GenesisInput
                    v-model="gitConfig.backendRepositoryName"
                    type="text"
                    label="Nom du dépôt Backend"
                    placeholder="mon-projet-api"
                    is-mandatory
                    fill-width
                />
                <GenesisInput
                    v-model="gitConfig.frontendRepositoryName"
                    type="text"
                    label="Nom du dépôt Frontend"
                    placeholder="mon-projet-ui"
                    is-mandatory
                    fill-width
                />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '../../composables/useGenerator';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';

const { stepperData } = useGenerator();

// Accès réactif à la configuration Git
const gitConfig = computed(() => stepperData.value.git);
</script>