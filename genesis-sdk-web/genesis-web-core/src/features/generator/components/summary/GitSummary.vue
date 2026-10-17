<template>
  <!-- Image toujours à droite : rangée non repliable, colonne droite à largeur fixe (shrink-0) -->
  <main class="flex flex-row gap-12 h-full w-full p-8">

    <!-- ═══ GAUCHE : informations, de la plus importante à la moins importante ═══ -->
    <div class="flex-1 min-w-0 min-h-0 overflow-y-auto custom-scrollbar flex flex-col">
      <div class="my-auto flex flex-col gap-10 py-4">

        <!-- 1. Stratégie (H1) -->
        <header class="flex flex-col gap-2">
          <span class="text-xs font-semibold uppercase tracking-widest text-muted">Git</span>
          <h1 class="text-5xl font-bold leading-tight text-text break-words">
            {{ strategy.title }}
          </h1>
          <p class="text-sm text-text-muted">{{ strategy.description }}</p>
        </header>

        <!-- 2. Détails du dépôt distant (uniquement s'il y en a un) -->
        <section v-if="featureGroups.length" class="flex flex-col gap-8">
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
    <div class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center gap-2 text-muted border border-secondary border-dashed bg-secondary/10 rounded-lg">
      <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M6 3v12M18 9a3 3 0 100-6 3 3 0 000 6zM6 21a3 3 0 100-6 3 3 0 000 6zM18 9a9 9 0 01-9 9"/>
      </svg>
      <span class="text-sm font-medium">Aperçu des dépôts</span>
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

const { stepperData } = useGenerator();

const git = computed(() => stepperData.value.git);

const or = (value: string | undefined | null, fallback = 'Non défini') => value || fallback;

// Trois cas possibles : étape passée (Git désactivé), local seul, local + distant
const strategy = computed(() => {
  if (!git.value.useGit) {
    return { title: 'Sans Git', description: 'Aucun dépôt Git ne sera initialisé dans le projet.' };
  }
  if (!git.value.useRemoteRepo) {
    return {
      title: 'Dépôt local',
      description: 'Seul un dépôt Git local (.git) sera initialisé dans le répertoire du projet.',
    };
  }
  return {
    title: 'Local + distant',
    description: 'Le projet sera initialisé localement et lié à un dépôt distant.',
  };
});

interface FeatureField { label: string; value: string }
interface FeatureGroup { id: string; gridClass: string; valueClass: string; fields: FeatureField[] }

// Même mécanique que les autres résumés : la hiérarchie vient de `valueClass` et `gridClass`.
const featureGroups = computed<FeatureGroup[]>(() => {
  const g = git.value;
  if (!g.useGit || !g.useRemoteRepo) return [];

  // Un seul dépôt, ou deux (backend + frontend)
  const repositories: FeatureField[] = g.separateRepositories
    ? [
        { label: 'Dépôt Backend',  value: or(g.backendRepositoryName) },
        { label: 'Dépôt Frontend', value: or(g.frontendRepositoryName) },
      ]
    : [{ label: 'Dépôt', value: or(g.repositoryName) }];

  return [
    {
      id: 'repositories',
      gridClass: 'grid-cols-2',
      valueClass: 'text-xl font-semibold font-mono',
      fields: repositories,
    },
    {
      id: 'account',
      gridClass: 'grid-cols-2',
      valueClass: 'text-base font-medium font-mono',
      fields: [
        { label: 'Utilisateur GitHub',    value: or(g.githubUsername) },
        { label: 'Personal Access Token', value: g.githubToken ? '••••••••••••••••' : 'Non fourni' },
      ],
    },
    {
      id: 'advanced',
      gridClass: 'grid-cols-2',
      valueClass: 'text-sm',
      fields: [
        { label: 'Structure',        value: g.separateRepositories ? 'Multi-repo (Frontend & Backend)' : 'Mono-repo' },
        { label: 'Dépôt distant',    value: g.isNewRemoteRepo ? 'Nouveau (créé à la génération)' : 'Existant' },
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