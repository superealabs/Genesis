<!-- genesis-sdk-web/genesis-web-core/src/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue -->
<template>
    <!-- ✅ On l'utilise TOUJOURS. Si tutorialId est undefined, il agit comme un simple <slot /> -->
    <GenesisTutorialItem :id="tutorialId">
        <div class="flex flex-col w-full min-w-[200px] max-w-[400px] gap-4 relative">
            <div class="relative">
                <GenesisInput
                    :modelValue="searchQuery"
                    @update:modelValue="handleSearchUpdate"
                    type="text"
                    placeholder="Configuration name....."
                    size="md"
                    fill-width
                >
                    <template #left>
                        <IconSearch :size="18" class="text-text-muted" />
                    </template>
                </GenesisInput>
            </div>

            <div class="flex flex-1 min-h-0 gap-4 overflow-hidden">
                <GenesisContainer
                    :min-height="rightColumnHeight"
                    :height="rightColumnHeight"
                    :max-height="maxSize()"
                    :is-empty="filteredConfigs.length === 0"
                >
                    <div class="p-3 space-y-2">
                        <GenesisItemConfig
                            v-for="config in filteredConfigs"
                            :key="config.id"
                            :name="config.name"
                            :is-hidden="config.isHidden"
                            :is-selected="selectedConfigId === config.id"
                            :show-delete="false" 
                            :readonly="readonly"
                            @select="handleSelect(config.id)"
                            @rename="(newName) => $emit('rename', config.id, newName)" 
                            @edit="$emit('edit', config.id)"
                            @toggleVisibility="handleToggleVisibility(config.id)"
                        />
                    </div>

                    <template #empty>
                        <div class="flex flex-col items-center justify-center h-32 text-text-muted text-sm">
                            <IconSearch :size="24" class="mb-2 opacity-50" />
                            <span>Aucune configuration trouvée.</span>
                        </div>
                    </template>
                </GenesisContainer>

                <div 
                    v-if="!readonly" 
                    ref="rightColumnRef" 
                    class="flex flex-col gap-2 flex-shrink-0 justify-between pt-1"
                >
                    <div class="flex flex-col gap-2">
                        <GenesisButtonIcon size="lg" title="Ajouter une configuration" variant="secondary" @click="handleAdd">
                            <IconPlus />
                        </GenesisButtonIcon>
                        <GenesisButtonIcon size="lg" title="Supprimer la configuration sélectionnée" :disabled="!selectedConfigId" class="disabled:opacity-50 disabled:cursor-not-allowed" @click="handleDelete">
                            <IconTrashAlt />
                        </GenesisButtonIcon>
                    </div>
                    <div class="flex flex-col gap-2">
                        <GenesisButtonIcon size="lg" title="Monter la configuration" :disabled="!canMoveUp" class="disabled:opacity-50 disabled:cursor-not-allowed" @click="handleMoveUp">
                            <IconChevronUp />
                        </GenesisButtonIcon>
                        <GenesisButtonIcon size="lg" title="Descendre la configuration" :disabled="!canMoveDown" class="disabled:opacity-50 disabled:cursor-not-allowed" @click="handleMoveDown">
                            <IconChevronDown />
                        </GenesisButtonIcon>
                    </div>
                </div>
            </div>
        </div>
    </GenesisTutorialItem>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'; // ✅ AJOUTÉ : ref, onMounted, nextTick
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconTrashAlt from '@genesis-labs/web-core/core/components/ui/icons/IconTrashAlt.vue';
import IconChevronUp from '@genesis-labs/web-core/core/components/ui/icons/IconChevronUp.vue';
import IconChevronDown from '@genesis-labs/web-core/core/components/ui/icons/IconChevronDown.vue';
import GenesisItemConfig from './GenesisItemConfig.vue';
import GenesisContainer from '../../GenesisContainer.vue';

const props = defineProps<{
    configurations: { id: string | number; name: string; isHidden?: boolean }[];
    selectedConfigId: string | number | null;
    filteredConfigs: { id: string | number; name: string; isHidden?: boolean }[];
    searchQuery: string;
    canMoveUp: boolean;
    canMoveDown: boolean;
    readonly?: boolean;
    tutorialId?: string;
}>();

const emit = defineEmits<{
    'update:searchQuery': [value: string];
    add: [];
    delete: [id: string | number];
    rename: [id: string | number, newName: string];
    edit: [id: string | number];
    toggleVisibility: [id: string | number];
    moveUp: [id: string | number];
    moveDown: [id: string | number];
    selectConfiguration: [id: string | number];
}>();

function handleSelect(id: string | number) { emit('selectConfiguration', id); }
function handleToggleVisibility(id: string | number) { emit('toggleVisibility', id); }
function handleAdd() { emit('add'); }
function handleDelete() { if (props.selectedConfigId !== null) emit('delete', props.selectedConfigId); }
function handleMoveUp() { if (props.selectedConfigId !== null) emit('moveUp', props.selectedConfigId); }
function handleMoveDown() { if (props.selectedConfigId !== null) emit('moveDown', props.selectedConfigId); }
function handleSearchUpdate(value: string | number | boolean) { emit('update:searchQuery', String(value)); }

// AJOUTÉ : Références pour le calcul dynamique de la hauteur
const rightColumnRef = ref<HTMLElement | null>(null);
const rightColumnHeight = ref(150); // Valeur de repli par défaut

const updateRightColumnHeight = () => {
    if (rightColumnRef.value) {
        // offsetHeight inclut le contenu, le padding et les bordures, ce qui correspond parfaitement à l'empreinte visuelle
        rightColumnHeight.value = rightColumnRef.value.offsetHeight;
    }
};

// On calcule la hauteur une fois que le DOM est entièrement rendu
onMounted(async () => {
    await nextTick();
    updateRightColumnHeight();
});

const maxSize = () => window.innerHeight * 0.85; 
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>