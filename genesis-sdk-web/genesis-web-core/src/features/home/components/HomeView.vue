<template>
    <div class="min-h-full flex flex-col gap-8 px-24 py-12">

        <!-- ═══ HEADER ═══ -->
        <header class="flex items-center justify-between">
            <div id="Navbar" class="flex items-center gap-2">
                <!-- Emplacement de la future navbar -->
            </div>

            <div class="flex items-center gap-2">
                <TabGroup
                    :selectedIndex="selectedIndex"
                    @change="onChange"
                    as="div"
                    class="inline-flex"
                >
                    <TabList
                        class="relative inline-flex rounded-full p-1 bg-neutral-genesis/10"
                        role="group"
                    >
                        <!-- Indicateur glissant : positionné sur le bouton actif -->
                        <span
                            aria-hidden="true"
                            class="absolute top-1 bottom-1 rounded-full bg-bg-neutral-genesis
                                transition-transform duration-300 ease-out"
                            :style="indicatorStyle"
                        />

                        <Tab
                            v-for="(option) in options"
                            :key="option.value"
                            as="template"
                            v-slot="{ selected }"
                        >
                            <button
                                type="button"
                                class="relative z-10 inline-flex items-center justify-center
                                    w-10 h-10 rounded-full
                                    transition-colors duration-200
                                    focus:outline-none focus-visible:ring-2"
                                :class="selected ? 'text-text' : 'text-text-muted hover:text-text'"
                            >
                                <component :is="option.icon" class="w-5 h-5" />
                                <span class="sr-only">{{ option.ariaLabel }}</span>
                            </button>
                        </Tab>
                    </TabList>
                </TabGroup>
            </div>
        </header>

        <main id="Hero Section" class="flex flex-col gap-24">

            <!-- ═══ TITRE CENTRAL ═══ -->
            <h1 class="text-center text-9xl md:text-8xl font-semibold leading-tight text-text">
                Welcome into
                <br/>
                <span class="text-primary">Genesis</span>
            </h1>

            <!-- ═══ CONTENU : texte à gauche, images à droite ═══ -->
            <div class="flex flex-col lg:flex-row lg:justify-around gap-8 lg:items-center">

                <div class="w-fit lg:w-1/3 flex flex-col gap-8">
                    <div class="flex flex-col gap-4">
                        <h2 class="text-5xl font-semibold leading-tight text-text">
                            Start to Generate any project any time
                        </h2>
                        <p class="text-sm text-text-muted leading-relaxed">
                            Générez votre projet pas à pas : choisissez votre framework,
                            votre base de données et votre dépôt, Genesis s'occupe du reste.
                        </p>
                    </div>

                    <GenesisTutorialItem id="create-project">
                        <GenesisButton
                            variant="accent"
                            size="lg"
                            class="self-start"
                            :disabled="isInitializing"
                            @click="handleCreateProject"
                        >
                            <template #leftIcon><IconFolderPlus /></template>
                            {{ isInitializing ? 'Initialisation...' : 'Créer un nouveau projet' }}
                        </GenesisButton>
                    </GenesisTutorialItem>
                </div>

                <div id="picture" class="w-fit lg:w-2/3 flex items-end gap-4 justify-end">
                    <div id="picture" class="w-full flex items-end gap-4">
                        <!-- Image A : grande -->
                        <div>
                            <figure id="pictureA" class="relative overflow-hidden rounded-xl bg-bg-light w-[100%] h-96 aspect-square">
                                <div
                                    role="img"
                                    aria-label="Image A"
                                    class="absolute inset-0 flex items-center justify-center p-2
                                        text-xs text-text-muted text-center border border-dashed border-secondary rounded-xl"
                                >
                                    Image A
                                </div>
                            </figure>
                            <div>
                                Texte
                            </div>
                        </div>

                        <!-- Image B : moyenne -->
                        <figure id="pictureB" class="relative overflow-hidden rounded-xl bg-bg-light w-[60%] h-64 aspect-square">
                            <div
                                role="img"
                                aria-label="Image B"
                                class="absolute inset-0 flex items-center justify-center p-2
                                    text-xs text-text-muted text-center border border-dashed border-secondary rounded-xl"
                            >
                                Image B
                            </div>
                        </figure>

                        <!-- Image C : petite, alignée en haut -->
                        <figure id="pictureC" class="relative overflow-hidden rounded-xl bg-bg-light w-[60%] h-64 self-start aspect-square">
                            <div
                                role="img"
                                aria-label="Image C"
                                class="absolute inset-0 flex items-center justify-center p-2
                                    text-xs text-text-muted text-center border border-dashed border-secondary rounded-xl"
                            >
                                Image C
                            </div>
                        </figure>
                    </div>
                </div>
            </div>
        </main>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
// import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import IconFolderPlus from '@genesis-labs/web-core/core/components/ui/icons/IconFolderPlus.vue';
// import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';
import IconSun from '@genesis-labs/web-core/core/components/ui/icons/IconSun.vue';
import IconMoon from '@genesis-labs/web-core/core/components/ui/icons/IconMoon.vue';
// import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { TabGroup, TabList, Tab } from '@headlessui/vue';

const emit = defineEmits<{
    'open-generator': [];
    'navigate': [routeName: string];
}>();

const { initializeNewProject, isInitializing } = useGenerator();

const themeMode = ref<'light' | 'dark'>('dark');

// Interface seulement pour le moment : aucun thème n'est réellement appliqué
const options = [
    { value: 'light' as const, icon: IconSun,  ariaLabel: 'Passer en mode clair' },
    { value: 'dark'  as const, icon: IconMoon, ariaLabel: 'Passer en mode sombre' },
];

const selectedIndex = computed(() =>
    Math.max(0, options.findIndex(o => o.value === themeMode.value))
);

function onChange(index: number) {
    const option = options[index];
    if (option) themeMode.value = option.value;
}

// Largeur d'un bouton = w-10 (40px) + gap éventuel ; ici pas de gap → 40px
const BUTTON_SIZE = 40;
const indicatorStyle = computed(() => ({
    width: `${BUTTON_SIZE}px`,
    transform: `translateX(${selectedIndex.value * BUTTON_SIZE}px)`,
}));

async function handleCreateProject() {
    const ok = await initializeNewProject();
    if (ok) emit('open-generator');
    // Si !ok, l'erreur est déjà dans wizardError (affichée par le parent via ErrorPopup)
}

// const tutorial = useGenesisTutorial('home', {
//     'main-menu': [
//         { id: 'create-project', text: 'Commencez ici : le générateur vous guide pas à pas pour créer un nouveau projet.' }
//     ]
// });
</script>

        <!-- <GenesisTutorialItem id="create-project">
            <GenesisButton 
                @click="handleCreateProject" 
                :variant="'accent'"
                :disabled="isInitializing"
            >
                <template #leftIcon><IconFolderPlus /></template>
                {{ isInitializing ? 'Initialisation...' : 'Créer un nouveau projet' }}
            </GenesisButton>
        </GenesisTutorialItem>

        <GenesisButton variant="secondary" @click="emit('navigate', 'design-system')">
            Design System
            <template #rightIcon>
                <IconChevronDown/>
            </template>
        </GenesisButton>

        <GenesisTutorialItem id="frameworks">
            <GenesisButton variant="primary" @click="emit('navigate', 'frameworks')">
                Frameworks
            </GenesisButton>
        </GenesisTutorialItem>

        <GenesisButton variant="primary" @click="emit('navigate', 'frontend')">
            Frontend
        </GenesisButton>

        <GenesisTutorialItem id="database">
            <GenesisButton variant="primary" @click="emit('navigate', 'database')">
                Database
            </GenesisButton>
        </GenesisTutorialItem>

        <GenesisButton variant="primary" @click="emit('navigate', 'repository')">
            Repository
        </GenesisButton>         -->