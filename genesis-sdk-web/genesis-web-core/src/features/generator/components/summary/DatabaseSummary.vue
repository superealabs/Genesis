<template>
  <!-- Image toujours à droite : rangée non repliable, colonne droite à largeur fixe (shrink-0) -->
  <main class="flex flex-row gap-12 h-full w-full p-8">

    <!-- ═══ GAUCHE : informations, de la plus importante à la moins importante ═══ -->
    <div class="flex-1 min-w-0 min-h-0 overflow-y-auto custom-scrollbar flex flex-col">
      <div class="my-auto flex flex-col gap-10 py-4">

        <!-- 1. Nom du moteur (H1) -->
        <header class="flex flex-col gap-2">
          <span class="text-xs font-semibold uppercase tracking-widest text-muted">Base de données</span>
          <h1 class="text-5xl font-bold leading-tight text-text break-words">
            {{ engineDisplayName }}
          </h1>
        </header>

        <!-- 2. Fonctionnalités : Credentials & Database Information -->
        <section class="flex flex-col gap-8">
          <dl
            v-for="group in featureGroups"
            :key="group.id"
            class="grid gap-x-10 gap-y-5"
            :class="group.gridClass"
          >
            <div v-for="field in group.fields" :key="field.label" class="flex flex-col gap-1 min-w-0">
              <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ field.label }}</dt>
              <dd class="text-text break-words" :class="group.valueClass">{{ field.value }}</dd>
            </div>
          </dl>
        </section>

      </div>
    </div>

    <!-- ═══ DROITE : visuel (statique, ne rétrécit jamais sous sa largeur) ═══ -->
    <div 
      class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center rounded-lg overflow-hidden relative min-h-[300px]" 
      :class="databaseIllustration ? '' : 'border border-secondary border-dashed bg-secondary/10'"
    >
      
      <!-- ✅ 1. Si l'image est présente : elle couvre tout l'espace disponible -->
      <img 
        v-if="databaseIllustration"
        :src="databaseIllustration" 
        alt="Illustration Base de données" 
        class="absolute inset-0 w-full h-full object-cover"
      />

      <!-- ✅ 2. Sinon (v-else), on affiche le placeholder générique (centré par le flex du parent) -->
      <template v-else>
        <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01"/>
        </svg>
        <span class="text-sm font-medium mt-2">Aperçu de la connexion</span>
      </template>

    </div>

  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useDatabaseStore } from '@genesis-labs/web-core/features/database/store/useDatabase.store';

// ✅ IMPORT DE L'ILLUSTRATION BASE DE DONNÉES
import databaseIllustration from '../../assets/database.png';

const { stepperData } = useGenerator();
const databaseStore = useDatabaseStore();

const database = computed(() => stepperData.value.database);

// Utilitaire pour les valeurs par défaut
const or = (value: string | number | undefined | null, fallback = 'Non défini') => {
  if (value === undefined || value === null || value === '') return fallback;
  return String(value);
};

// Résolution du nom du SGBD
const engineDisplayName = computed(() => {
  const engKey = database.value.engine;
  if (!engKey) return 'Moteur non spécifié';

  const found = databaseStore.availableEngines.find(e => {
    const normalizedStoreName = e.name.toLowerCase().replace(/\s+/g, '');
    return normalizedStoreName === engKey || e.name.toLowerCase() === engKey;
  });

  return found ? found.name : engKey.charAt(0).toUpperCase() + engKey.slice(1);
});

// Interfaces pour la structure des groupes
interface FeatureField { label: string; value: string }
interface FeatureGroup { id: string; gridClass: string; valueClass: string; fields: FeatureField[] }

// Une seule source de vérité pour le rendu, alignée sur DatabaseConfigView.vue
const featureGroups = computed<FeatureGroup[]>(() => {
  const db = database.value;
  const isOracle = db.engine === 'oracle';
  const isPostgreOrSql = db.engine === 'postgre' || db.engine === 'sqlserver';
  const isMysql = db.engine === 'mysql';

  // Calcul de l'URL JDBC pour l'affichage (comme dans la vue de config)
  const hostStr = db.host || 'localhost';
  const portStr = db.port || '';
  const dbName = db.databaseName || '';
  
  let jdbcUrl = 'Non défini';
  switch (db.engine) {
    case 'mysql': jdbcUrl = `jdbc:mysql://${hostStr}:${portStr}/${dbName}`; break;
    case 'postgre': jdbcUrl = `jdbc:postgresql://${hostStr}:${portStr}/${dbName}`; break;
    case 'sqlserver': jdbcUrl = `jdbc:sqlserver://${hostStr}:${portStr};databaseName=${dbName}`; break;
    case 'oracle': jdbcUrl = `jdbc:oracle:thin:@${hostStr}:${portStr}:${db.sid || dbName}`; break;
  }

  return [
    {
      id: 'connection',
      gridClass: 'grid-cols-2',
      valueClass: 'text-xl font-semibold',
      fields: [
        { label: 'Hôte', value: or(db.host) },
        { label: 'Port', value: or(db.port) },
        { label: 'Nom de la base', value: or(db.databaseName) },
        // Conditionnel : SID uniquement pour Oracle
        ...(isOracle ? [{ label: 'SID', value: or(db.sid) }] : []),
        // Conditionnel : Schéma uniquement pour Postgres et SQL Server
        ...(isPostgreOrSql ? [{ label: 'Schéma', value: or(db.schema) }] : []),
      ],
    },
    {
      id: 'credentials',
      gridClass: 'grid-cols-2',
      valueClass: 'text-xl font-semibold',
      fields: [
        { label: 'Utilisateur', value: or(db.username) },
        { label: 'Mot de passe', value: db.password ? '••••••••' : 'Non défini' },
      ],
    },
    {
      id: 'advanced',
      gridClass: 'grid-cols-2',
      valueClass: 'text-sm',
      fields: [
        { label: 'URL de connexion', value: jdbcUrl },
        { label: 'Type de Driver', value: or(db.driverType) },
        { label: 'Nom du Driver', value: or(db.driverName) },
        // Conditionnel : Allow public key retrieval uniquement pour MySQL
        ...(isMysql ? [{ label: 'Allow public key retrieval', value: db.allowPublicKeyRetrieval ? 'Oui' : 'Non' }] : []),
        { label: 'Trust certificate', value: db.trustCertificate ? 'Oui' : 'Non' },
      ],
    },
  ];
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
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