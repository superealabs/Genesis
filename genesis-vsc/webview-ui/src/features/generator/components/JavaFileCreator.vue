<template>
    <BaseFormPopup title="créer un fichier java" @close="$emit('close')">
        <div class="p-4 flex flex-col gap-4">
            <h2 class="text-sm font-semibold text-text">Créer un fichier Java</h2>

            <!-- Nom de la classe -->
            <div class="flex flex-col gap-1">
                <label class="text-xs text-text-muted">Nom de la classe</label>
                <input
                    v-model="className"
                    type="text"
                    placeholder="MyClass"
                    class="bg-bg-light text-text border border-secondary rounded px-3 py-1.5 text-sm
                        focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent"
                />
            </div>

            <!-- Répertoire de destination -->
            <div class="flex flex-col gap-1">
                <label class="text-xs text-text-muted">Répertoire de destination</label>
                <div class="flex gap-2">
                    <input
                        v-model="destinationPath"
                        type="text"
                        placeholder="C:/mon/projet/src"
                        class="flex-1 bg-bg-light text-text border border-secondary rounded px-3 py-1.5 text-sm
                            focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent"
                    />
                    <GenesisButton variant="secondary" @click="browseFolder">
                        <!-- <template #leftIcon><IconFolderOpen /></template> -->
                        Parcourir
                    </GenesisButton>
                </div>
            </div>

            <!-- Résultat -->
            <div
                v-if="result"
                class="px-3 py-2 rounded text-xs"
                :class="result.success === 'true' ? 'bg-accent/20 text-accent' : 'bg-red-500/20 text-red-400'"
            >
                {{ result.message }}
            </div>

            <!-- Bouton créer -->
            <GenesisButton
                :disabled="!className || !destinationPath || isLoading"
                @click="createFile"
            >
                <!-- <template #leftIcon><IconPlus /></template> -->
                {{ isLoading ? 'Création en cours...' : 'Créer le fichier .java' }}
            </GenesisButton>
        </div>
    </BaseFormPopup>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useVsCode } from '@/core/composables/useVsCode';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue';
// import IconFolderOpen from '@/core/components/ui/icons/IconFolderOpen.vue';
// import IconPlus from '@/core/components/ui/icons/IconPlus.vue';

const { send, onMessage } = useVsCode();

const emit = defineEmits<{
    close: [];
}>();

const className = ref('');
const destinationPath = ref('');
const isLoading = ref(false);
const result = ref<{ success: string; message: string } | null>(null);

let cleanup: () => void;

onMounted(() => {
    cleanup = onMessage((message) => {
        if (message.type === 'generateResult') {
            const payload = message.payload as { success: string; message: string };
            result.value = payload;
            isLoading.value = false;
        }

        if (message.type === 'folderSelected') {
            destinationPath.value = message.payload as string;
        }
    });

    send('ready');
});

onUnmounted(() => cleanup?.());

function createFile() {
    if (!className.value || !destinationPath.value) return;
    isLoading.value = true;
    result.value = null;
    send('generateJavaFile', {
        className: className.value,
        destinationPath: destinationPath.value
    });
}

function browseFolder() {
    send('browseFolder');
}
</script>