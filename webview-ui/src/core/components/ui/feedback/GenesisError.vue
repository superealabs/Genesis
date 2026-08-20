<template>
    <div class="flex flex-col items-center justify-center gap-3 w-full h-full">

        <div class="flex flex-col gap-3 max-w-md w-full">
            <!-- Message d'erreur -->
            <div class="flex justify-center gap-2">
                <span class="text-red-400 text-lg text-center leading-none">⚠</span>
                <p class="text-sm text-text">{{ message }}</p>
            </div>

            <!-- Stack trace (optionnel) -->
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
import { ref } from 'vue';

defineProps<{
    message: string;
    stackTrace?: string;
    showStackTrace?: boolean;
}>();

const isStackOpen = ref(false);
</script>