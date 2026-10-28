<template>
    <div class="relative h-full w-full overflow-hidden">
        <!-- Zone des slides : on laisse de la place à droite pour la frise -->
        <div ref="viewport" class="absolute inset-y-0 left-0 right-16">
            <!-- `invisible` : tous cachés au départ, GSAP révèle le slide actif (style inline prioritaire) -->
            <section
                v-for="slide in SUMMARY_SLIDES"
                :key="slide.id"
                class="absolute inset-0 invisible overflow-y-auto"
            >
                <component :is="slide.component" />
            </section>
        </div>

        <!-- ✅ CONTENEUR GLOBAL DE LA FRISE + DÉCLENCHEUR -->
        <GenesisTutorialItem id="summary-rail-global">
            <div class="absolute right-4 top-1/2 -translate-y-1/2 flex flex-col items-center gap-3">
                
                <!-- ✅ TRIGGER AU-DESSUS DE TOUTES LES ÉTAPES -->
                <GenesisButtonIcon 
                    variant="neutral" 
                    size="sm" 
                    @click="tutorial.restart('main')"
                    title="Lancer le tutoriel du résumé"
                >
                    <IconHelp />
                </GenesisButtonIcon>
                
                <!-- La frise (qui contient déjà le GenesisTutorialItem de l'étape active) -->
                <SummaryStepRail
                    :labels="labels"
                    :current="index"
                    @select="handleRailSelect"
                />
            </div>
        </GenesisTutorialItem>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import SummaryStepRail from '../summary/SummaryStepRail.vue';
import { SUMMARY_SLIDES } from '../summary/summary-slides.ts';
import { useSlideTransition } from '@genesis-labs/web-core/core/composables/ux/useSlideTransition';
import { useStepSlider } from '@genesis-labs/web-core/core/composables/ux/useStepSlider';
import { useStepInput } from '@genesis-labs/web-core/core/composables/ux/useStepInput';

// ✅ IMPORTS POUR LE TUTORIEL
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';

const labels = SUMMARY_SLIDES.map(s => s.label);

const viewport = ref<HTMLElement | null>(null);

// Assemblage : animation (GSAP) → état (index + verrou) → entrées (molette / clavier)
const { show, play } = useSlideTransition(viewport);
const { index, goTo, next, prev } = useStepSlider(SUMMARY_SLIDES.length, play);
useStepInput(viewport, { next, prev });

defineProps<{
    initialIndex?: number; // ✅ Reçoit l'index cible depuis le parent
}>();

const emit = defineEmits<{
    requestEdit: [summaryIndex: number]; // ✅ Signale qu'on veut éditer cette section
    'update-summary-complete': [isComplete: boolean];
}>();

function handleRailSelect(i: number) {
    if (i === index.value) {
        emit('requestEdit', i);
    } else {
        goTo(i);
    }
}

watch(index, (newIndex) => {
    // Le dernier slide est à l'index SUMMARY_SLIDES.length - 1
    const isLastSlide = newIndex === SUMMARY_SLIDES.length - 1;
    emit('update-summary-complete', isLastSlide);
}, { immediate: true });

onMounted(() => show(index.value));

// ✅ CONFIGURATION DU TUTORIEL (Ordre respecté : Global puis Actif)
const tutorial = useGenesisTutorial('summary-view', {
    'main': [
        { 
            id: 'summary-rail-global', 
            text: 'Cette frise vous permet de visualiser et de naviguer rapidement entre les différentes sections de votre configuration.' 
        },
        { 
            id: 'summary-rail-active-step', 
            text: 'Pour modifier une section spécifique, double-cliquez sur l\'étape actuellement active (mise en surbrillance).' 
        }
    ]
});
</script>