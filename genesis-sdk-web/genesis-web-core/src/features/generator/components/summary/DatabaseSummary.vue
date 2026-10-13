<template>
  <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
    <h3 class="text-lg font-semibold text-text mb-4">Résumé de la Base de Données</h3>

    <!-- Section 1 : Moteur et Connexion -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01"/>
        </svg>
        Moteur et Connexion
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">SGBD</span>
            <p class="text-sm font-medium text-text capitalize">{{ engineDisplayName }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Host IP</span>
            <p class="text-sm font-medium text-text">{{ database.host || 'Non défini' }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Port</span>
            <p class="text-sm font-medium text-text">{{ database.port || 'Non défini' }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 2 : Base de Données et Identifiants -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
        </svg>
        Base de Données et Identifiants
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Nom de la base</span>
            <p class="text-sm font-medium text-text">{{ database.databaseName || 'Non défini' }}</p>
          </div>
          
          <!-- Conditionnel : Schema (PostgreSQL / SQL Server) -->
          <div v-if="(database.engine === 'postgre' || database.engine === 'sqlserver') && database.schema" class="space-y-1">
            <span class="text-xs text-muted">Schema</span>
            <p class="text-sm font-medium text-text">{{ database.schema }}</p>
          </div>

          <!-- Conditionnel : SID (Oracle) -->
          <div v-if="database.engine === 'oracle' && database.sid" class="space-y-1">
            <span class="text-xs text-muted">SID</span>
            <p class="text-sm font-medium text-text">{{ database.sid }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Username</span>
            <p class="text-sm font-medium text-text">{{ database.username || 'Non défini' }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Password</span>
            <!-- Masqué par sécurité dans le résumé -->
            <p class="text-sm font-medium text-text">{{ database.password ? '••••••••' : 'Non défini' }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 3 : Configuration Avancée -->
    <section class="space-y-4">
      <h4 class="text-base font-semibold text-text flex items-center gap-2">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
        Configuration Avancée
      </h4>
      
      <div class="bg-bg-secondary/30 rounded-lg p-4 border border-neutral-light space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-1">
            <span class="text-xs text-muted">Driver Type</span>
            <p class="text-sm font-medium text-text font-mono text-xs">{{ database.driverType || 'Non défini' }}</p>
          </div>
          
          <div class="space-y-1">
            <span class="text-xs text-muted">Driver Name</span>
            <p class="text-sm font-medium text-text">{{ database.driverName || 'Non défini' }}</p>
          </div>
        </div>

        <!-- Séparateur -->
        <div class="border-t border-neutral-light"></div>

        <!-- Options Booléennes Conditionnelles -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-if="database.engine === 'mysql'" class="flex items-center gap-2">
            <div class="w-4 h-4 rounded border flex items-center justify-center" 
                 :class="database.allowPublicKeyRetrieval ? 'bg-primary border-primary' : 'border-secondary bg-bg'">
              <svg v-if="database.allowPublicKeyRetrieval" class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/>
              </svg>
            </div>
            <span class="text-sm text-text">Allow public key retrieval</span>
          </div>

          <div class="flex items-center gap-2">
            <div class="w-4 h-4 rounded border flex items-center justify-center" 
                 :class="database.trustCertificate ? 'bg-primary border-primary' : 'border-secondary bg-bg'">
              <svg v-if="database.trustCertificate" class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/>
              </svg>
            </div>
            <span class="text-sm text-text">Trust certificate</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useDatabaseStore } from '@genesis-labs/web-core/features/database/store/useDatabase.store';

const { stepperData } = useGenerator();
const databaseStore = useDatabaseStore();

const database = computed(() => stepperData.value.database);

// ✅ RÉSOLUTION DU NOM DU SGBD
// On normalise le nom du moteur disponible dans le store pour le comparer à la clé stockée
const engineDisplayName = computed(() => {
  const engKey = database.value.engine;
  if (!engKey) return 'Non spécifié';

  const found = databaseStore.availableEngines.find(e => {
    const normalizedStoreName = e.name.toLowerCase().replace(/\s+/g, '');
    return normalizedStoreName === engKey || e.name.toLowerCase() === engKey;
  });

  // Fallback : si on ne trouve pas dans le store, on capitalise la clé (ex: 'postgre' -> 'Postgre')
  return found ? found.name : engKey.charAt(0).toUpperCase() + engKey.slice(1);
});
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