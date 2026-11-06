<template>
    <div class="flex flex-col items-center justify-center gap-10 w-full h-full overflow-auto">

        <img
            :src="ErrorPicture"
            alt="Erreur"
            class="w-1/2 max-w-full h-auto object-contain"
        />

        <div class="flex flex-col gap-3 max-w-md w-full">

            <!-- ═══ Cas 1 : message structuré ═══ -->
            <template v-if="isStructured">
                <!-- Titre + icône -->
                <div class="flex justify-center gap-2 text-lg">
                    <span class="text-red-400 text-lg leading-none shrink-0 mt-0.5">⚠</span>
                    <p class="text-sm font-semibold text-text">{{ structured.title }}</p>
                </div>

                <!-- Résumé -->
                <p v-if="structured.summary" class="text-sm text-text-muted pl-6">
                    {{ structured.summary }}
                </p>

                <!-- Détails -->
                <ul
                    v-if="structured.details?.length"
                    class="flex flex-col gap-1 pl-10 pr-2 text-xs text-text-muted"
                >
                    <li
                        v-for="(line, i) in structured.details"
                        :key="i"
                        class="list-disc marker:text-red-400/70"
                    >
                        {{ line }}
                    </li>
                </ul>
            </template>

            <!-- ═══ Cas 2 : message simple (string) — rétrocompatible ═══ -->
            <div v-else class="flex justify-center gap-2">
                <span class="text-red-400 text-lg leading-none shrink-0 mt-0.5">⚠</span>
                <p class="text-sm text-text text-center whitespace-pre-wrap pl-4 -indent-4">{{ message }}</p>
            </div>

            <!-- Stack trace (inchangé) -->
            <div v-if="showStackTrace && stackTrace" class="flex flex-col gap-1">
                <button
                    class="text-xs text-text-muted hover:text-text text-left cursor-pointer bg-transparent border-none"
                    @click="isStackOpen = !isStackOpen"
                >
                    {{ isStackOpen ? '▼' : '▶' }} Détails techniques
                </button>
                <pre
                    v-if="isStackOpen"
                    class="text-xs text-text-muted bg-bg-dark p-3 rounded overflow-x-auto whitespace-pre-wrap border border-secondary"
                >{{ stackTrace }}</pre>
            </div>
        </div>

    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import ErrorPicture from '../../../../assets/PICTURES/error.png';
import type { WizardError, WizardErrorPayload } from '@genesis-labs/web-core/features/generator/types/generator.types';

//  Ajuste le chemin d'import selon l'alias de ton projet
//    (ici on suppose '@genesis-labs/web-core/...')

const props = defineProps<{
    message: WizardError;    // ← accepte string OU objet
    stackTrace?: string;
    showStackTrace?: boolean;
}>();

// ── Détection du format ──
function isObjectMessage(m: WizardError): m is WizardErrorPayload {
    return typeof m === 'object' && m !== null;
}

const isStructured = computed(() => isObjectMessage(props.message));

const structured = computed<WizardErrorPayload>(() =>
    isObjectMessage(props.message) ? props.message : { title: '', details: [] }
);

const message = computed(() =>
    typeof props.message === 'string' ? props.message : ''
);

const isStackOpen = ref(false);
</script>