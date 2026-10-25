<template>
  <!-- Image toujours à droite : rangée non repliable, colonne droite à largeur fixe (shrink-0) -->
  <main class="flex flex-row gap-12 h-full w-full p-8">

    <!-- ═══ GAUCHE : informations, de la plus importante à la moins importante ═══ -->
    <div class="flex-1 min-w-0 min-h-0 overflow-y-auto custom-scrollbar flex flex-col">
      <div class="my-auto flex flex-col gap-10 py-4">

        <!-- 1. Nom du projet -->
        <header class="flex flex-col gap-2">
          <span class="text-xs font-semibold uppercase tracking-widest text-muted">Projet</span>
          <h1 class="text-5xl font-bold leading-tight text-text break-words">
            {{ config.projectName || 'Sans nom' }}
          </h1>
          <span class="text-xs text-muted break-all">{{ config.projectLocation || 'Localisation non définie' }}</span>
        </header>

        <!-- 2. Fonctionnalités : regroupées avec des entêtes clairs -->
        <section class="flex flex-col gap-12">
          
          <div v-for="group in featureGroups" :key="group.id" class="flex flex-col gap-4">
            
            <!-- ✅ NOUVEAU : Entête de section (s'affiche uniquement si un titre est défini) -->
            <div v-if="group.title" class="flex items-center gap-3 pb-2 border-b border-bg-neutral-genesis">
              <h3 class="text-md font-bold uppercase tracking-widest">
                {{ group.title }}
              </h3>
            </div>

            <dl class="grid gap-x-10 gap-y-5" :class="group.gridClass">
              <div v-for="field in group.fields" :key="field.label" class="flex flex-col gap-1 min-w-0">
                <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ field.label }}</dt>
                <dd class="text-text break-words" :class="group.valueClass">{{ field.value }}</dd>
                <!-- Petite ligne de séparation verticale optionnelle pour la grille 'build' si désiré, mais le gap suffit souvent -->
              </div>
            </dl>
          </div>

        </section>

        <!-- 3. Description : en dernier, rétractable (fermée par défaut) -->
        <details class="group mt-2">
          <summary
            class="flex items-center gap-2 cursor-pointer select-none list-none [&::-webkit-details-marker]:hidden text-xs font-semibold uppercase tracking-wider text-muted hover:text-text transition-colors"
          >
            Description
            <svg
              class="w-3 h-3 transition-transform duration-200 group-open:rotate-180"
              fill="none" stroke="currentColor" viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </summary>
          <p class="mt-3 w-full pr-4 text-sm leading-relaxed text-text whitespace-pre-line">
            {{ config.projectDescription || 'Aucune description' }}
          </p>
        </details>

      </div>
    </div>

    <!-- ═══ DROITE : visuel (statique, ne rétrécit jamais sous sa largeur) ═══ -->
    <div 
      class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center rounded-lg overflow-hidden relative min-h-[300px]" 
      :class="projectIllustration ? '' : 'border border-secondary border-dashed bg-secondary/10'"
    >
      <img 
        v-if="projectIllustration"
        :src="projectIllustration" 
        alt="Project" 
        class="absolute inset-0 w-full h-full object-cover"
      />
      <template v-else>
        <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
        <span class="text-sm font-medium">Aperçu du projet</span>
      </template>
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useFrameworkStore } from '@genesis-labs/web-core/features/frameworks/store/useFramework.store';
import projectIllustration from '../../assets/project-illustration.png'; // Assure-toi que le chemin est bon

const { stepperData } = useGenerator();
const frameworkStore = useFrameworkStore();

const framework = computed(() => stepperData.value.framework);
const config = computed(() => stepperData.value.config);

const languageName = computed(() => {
  if (!framework.value?.languageId) return 'Non spécifié';
  const lang = frameworkStore.languages.find(l => l.id === framework.value!.languageId);
  return lang ? lang.name : 'Inconnu';
});

const or = (value: string | undefined | null, fallback = 'Non défini') => value || fallback;

interface FeatureField { label: string; value: string }
interface FeatureGroup { 
  id: string; 
  title?: string; // ✅ NOUVEAU : Titre optionnel pour la section
  gridClass: string; 
  valueClass: string; 
  fields: FeatureField[] 
}

const featureGroups = computed<FeatureGroup[]>(() => [
  {
    id: 'stack',
    // Pas de titre pour la stack, car c'est l'information principale qui suit directement le nom du projet
    gridClass: 'grid-cols-2',
    valueClass: 'text-2xl font-semibold',
    fields: [
      { label: 'Framework', value: `${framework.value?.name ?? 'Aucun'} ${config.value.frameworkVersion ?? ''}`.trim() },
      { label: 'Langage',   value: `${languageName.value} ${config.value.languageVersion ?? ''}`.trim() },
    ],
  },
  {
    id: 'build',
    title: 'Configuration de Build', // ✅ AJOUTÉ
    gridClass: 'grid-cols-3',
    valueClass: 'text-base font-medium',
    fields: [
      { label: 'Build tool', value: or(config.value.buildTool) },
      { label: 'Group ID',   value: or(config.value.groupId) },
      { label: 'Port',       value: or(config.value.projectPort) },
    ],
  },
  {
    id: 'advanced',
    title: 'Options Avancées', // ✅ AJOUTÉ
    gridClass: 'grid-cols-2 lg:grid-cols-4',
    valueClass: 'text-sm',
    fields: [
      { label: 'Logging',  value: or(config.value.loggingLevel) },
      { label: 'Sécurité', value: or(config.value.securityType) },
      { label: 'Cache',    value: or(config.value.cacheProvider) },
      ...(config.value.hibernateDdlAuto
        ? [{ label: 'Hibernate DDL', value: config.value.hibernateDdlAuto }]
        : []),
    ],
  },
]);
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