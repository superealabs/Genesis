<template>
  <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
    <h3 class="text-lg font-semibold text-text mb-4">Résumé de la configuration</h3>

    <!-- Section Framework -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"/>
        </svg>
        Framework
      </h4>
      
      <div v-if="framework" class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Nom</span>
            <p class="text-sm font-medium text-text">{{ framework.name }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Langage</span>
            <p class="text-sm font-medium text-text">{{ framework.languageId }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Version</span>
            <p class="text-sm font-medium text-text">{{ config.frameworkVersion || 'Non spécifiée' }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Version du langage</span>
            <p class="text-sm font-medium text-text">{{ config.languageVersion || 'Non spécifiée' }}</p>
          </div>
        </div>
      </div>
      
      <div v-else class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light">
        <p class="text-sm text-muted italic">Aucun framework sélectionné</p>
      </div>
    </section>

    <!-- Section Configuration du Projet -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
        </svg>
        Configuration du Projet
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light space-y-4">
        <!-- Informations de base -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Nom du projet</span>
            <p class="text-sm font-medium text-text">{{ config.projectName || 'Non défini' }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Port</span>
            <p class="text-sm font-medium text-text">{{ config.projectPort || 'Non défini' }}</p>
          </div>
          
          <div class="space-y-1 md:col-span-2">
            <span class="text-xs text-muted">Localisation</span>
            <p class="text-sm font-medium text-text break-all">{{ config.projectLocation || 'Non définie' }}</p>
          </div>
          
          <div class="space-y-1 md:col-span-2">
            <span class="text-xs text-muted">Description</span>
            <p class="text-sm text-text">{{ config.projectDescription || 'Aucune description' }}</p>
          </div>
        </div>

        <!-- Séparateur -->
        <div class="border-t border-neutral-light"></div>

        <!-- Configuration technique -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Build Tool</span>
            <p class="text-sm font-medium text-text capitalize">{{ config.buildTool || 'Non défini' }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Group ID</span>
            <p class="text-sm font-medium text-text">{{ config.groupId || 'Non défini' }}</p>
          </div>
        </div>

        <!-- Séparateur -->
        <div class="border-t border-neutral-light"></div>

        <!-- Configuration avancée -->
        <div class="space-y-3">
          <h5 class="text-sm font-semibold text-text">Configuration Avancée</h5>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="space-y-1">
              <span class="text-xs text-muted">Niveau de Logging</span>
              <p class="text-sm font-medium text-text">{{ config.loggingLevel || 'Non défini' }}</p>
            </div>
            
            <div class="space-y-1">
              <span class="text-xs text-muted">Type de Sécurité</span>
              <p class="text-sm font-medium text-text">{{ config.securityType || 'Non défini' }}</p>
            </div>
            
            <div class="space-y-1">
              <span class="text-xs text-muted">Fournisseur de Cache</span>
              <p class="text-sm font-medium text-text">{{ config.cacheProvider || 'Non défini' }}</p>
            </div>
            
            <div v-if="config.hibernateDdlAuto" class="space-y-1">
              <span class="text-xs text-muted">Hibernate DDL Auto</span>
              <p class="text-sm font-medium text-text">{{ config.hibernateDdlAuto }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

const { stepperData } = useGenerator();

const framework = computed(() => stepperData.value.framework);
const config = computed(() => stepperData.value.config);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: var(--color-neutral-light);
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: var(--color-neutral);
}
</style>