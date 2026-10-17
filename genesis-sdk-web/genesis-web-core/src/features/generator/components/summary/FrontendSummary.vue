<template>
  <!-- Image toujours à droite : rangée non repliable, colonne droite à largeur fixe (shrink-0) -->
  <main class="flex flex-row gap-12 h-full w-full p-8">

    <!-- ═══ GAUCHE : informations, de la plus importante à la moins importante ═══ -->
    <div class="flex-1 min-w-0 min-h-0 overflow-y-auto custom-scrollbar flex flex-col">
      <div class="my-auto flex flex-col gap-10 py-4">

        <!-- 1. Framework frontend (H1) -->
        <header class="flex flex-col gap-2">
          <span class="text-xs font-semibold uppercase tracking-widest text-muted">Frontend</span>
          <h1 class="text-5xl font-bold leading-tight text-text break-words">
            {{ frontend?.name || 'Aucun frontend sélectionné' }}
          </h1>
        </header>

        <!-- 2. Fonctionnalités : exécution & interface → charte graphique → assets -->
        <section class="flex flex-col gap-8">
          <dl
            v-for="group in featureGroups"
            :key="group.id"
            class="grid gap-x-10 gap-y-5"
            :class="group.gridClass"
          >
            <div v-for="field in group.fields" :key="field.label" class="flex flex-col gap-1 min-w-0">
              <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ field.label }}</dt>
              <dd class="flex items-center gap-2 text-text min-w-0" :class="group.valueClass">
                <!-- Pastille de couleur : la couleur EST la donnée, pas de la décoration -->
                <span
                  v-if="field.swatch"
                  class="w-4 h-4 rounded-full shrink-0"
                  :style="{ backgroundColor: field.swatch }"
                />
                <span class="break-words min-w-0">{{ field.value }}</span>
              </dd>
            </div>
          </dl>
        </section>

      </div>
    </div>

    <!-- ═══ DROITE : visuel (statique, ne rétrécit jamais sous sa largeur) ═══ -->
    <div class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center gap-2 text-muted border border-secondary border-dashed bg-secondary/10 rounded-lg">
      <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
      </svg>
      <span class="text-sm font-medium">Aperçu de l'interface</span>
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';

const { stepperData } = useGenerator();
const frontendStore = useFrontendStore();

const frontend = computed(() => stepperData.value.frontend);
const layout = computed(() => stepperData.value.frontendLayout);

const or = (value: string | undefined | null, fallback = 'Non défini') => value || fallback;

// Code de langue → nom lisible (retombe sur le code si inconnu)
const getLanguageName = (code: string): string =>
  frontendStore.availableInterfaceLanguages.find(l => l.code === code)?.name ?? code;

// Chemin complet → nom du fichier
const getFileName = (path: string): string => path.split(/[\\/]/).pop() || path;

const NAVBAR_LABELS: Record<string, string> = {
  side: 'Barre latérale',
  top: 'Barre supérieure',
};

interface FeatureField { label: string; value: string; swatch?: string }
interface FeatureGroup { id: string; gridClass: string; valueClass: string; fields: FeatureField[] }

// Même mécanique que ProjectSummary / DatabaseSummary :
// le template boucle, la hiérarchie vient de `valueClass` (taille) et `gridClass` (colonnes).
const featureGroups = computed<FeatureGroup[]>(() => {
  const l = layout.value;
  const languages = l.selectedInterfaceLanguages.map(getLanguageName).join(' · ');

  return [
    {
      id: 'interface',
      gridClass: 'grid-cols-2',
      valueClass: 'text-xl font-semibold',
      fields: [
        { label: 'Port',      value: or(l.port) },
        { label: 'Navigation', value: NAVBAR_LABELS[l.navbarType] ?? 'Non défini' },
        { label: 'Langues',   value: or(languages, 'Aucune langue sélectionnée') },
      ],
    },
    {
      id: 'colors',
      gridClass: 'grid-cols-2',
      valueClass: 'text-base font-medium font-mono',
      fields: [
        { label: 'Couleur primaire',   value: or(l.primaryColor),   swatch: l.primaryColor   || undefined },
        { label: 'Couleur secondaire', value: or(l.secondaryColor), swatch: l.secondaryColor || undefined },
      ],
    },
    {
      id: 'assets',
      gridClass: 'grid-cols-2',
      valueClass: 'text-sm',
      fields: [
        { label: 'Logo',    value: l.logoPath    ? getFileName(l.logoPath)    : 'Non défini' },
        { label: 'Favicon', value: l.faviconPath ? getFileName(l.faviconPath) : 'Non défini' },
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