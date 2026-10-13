<!-- genesis-sdk-web/genesis-web-core/src/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue -->
<template>
    <div class="flex flex-col w-full min-w-[300px] max-w-[400px] gap-4 relative" :style="resizeStyle">
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
            <div class="flex-1 flex flex-col min-h-0 overflow-hidden border border-bg-neutral-genesis rounded-lg relative">
                <div class="flex-1 overflow-y-auto p-3 space-y-2 custom-scrollbar">
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

                    <div v-if="filteredConfigs.length === 0" class="flex flex-col items-center justify-center h-32 text-text-muted text-sm">
                        <IconSearch :size="24" class="mb-2 opacity-50" />
                        <span>Aucune configuration trouvée.</span>
                    </div>
                </div>
                <GenesisResizeHandle @resize-start="startResizeBottom" />
            </div>

            <!-- ✅ Masqué en mode readonly -->
            <div v-if="!readonly" class="flex flex-col gap-2 flex-shrink-0 justify-between pt-1">
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
</template>

<script setup lang="ts">
import { ref } from 'vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconTrashAlt from '@genesis-labs/web-core/core/components/ui/icons/IconTrashAlt.vue';
import IconChevronUp from '@genesis-labs/web-core/core/components/ui/icons/IconChevronUp.vue';
import IconChevronDown from '@genesis-labs/web-core/core/components/ui/icons/IconChevronDown.vue';
import GenesisItemConfig from './GenesisItemConfig.vue';
import { useResizable } from '@genesis-labs/web-core/core/composables/ux/useResizable';
import GenesisResizeHandle from '../../../ui/actions/GenesisResizeHandle.vue';

const props = defineProps<{
    configurations: { id: string | number; name: string; isHidden?: boolean }[];
    selectedConfigId: string | number | null;
    filteredConfigs: { id: string | number; name: string; isHidden?: boolean }[];
    searchQuery: string;
    canMoveUp: boolean;
    canMoveDown: boolean;
    readonly?: boolean; // ✅ AJOUTÉ
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

const { resizeStyle, startResizeBottom } = useResizable({
    minHeight: 250,
    maxHeight: () => window.innerHeight * 0.85,
    resizableX: ref(false),
    resizableY: ref(true)
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>