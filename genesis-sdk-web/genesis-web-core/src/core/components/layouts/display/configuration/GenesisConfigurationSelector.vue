<template>
    <div class="flex flex-col gap-3 w-full">
        
        <!-- ═══ PARTIE 1 : Le Panneau de Configuration (Générique) ═══ -->
        <GenesisConfigurationManageData
            :configurations="configurations"
            :selected-config-id="selectedConfigId"
            :filtered-configs="filteredConfigs"
            :search-query="searchQuery"
            :can-move-up="canMoveUp"
            :can-move-down="canMoveDown"
            :show-toggle-visibility="props.showToggleVisibility"
            :tutorial-id="props.panelTutorialId"
            :actions-tutorial-id="props.actionsDataTutorialId"
            :include-sub-tutorial="false"
            @update:search-query="(val) => $emit('update:searchQuery', val)"
            @add="$emit('add')"
            @delete="(id) => $emit('delete', id)"
            @rename="(id, newName) => $emit('rename', id, newName)"
            @edit="(id) => $emit('edit', id)"
            @toggle-visibility="(id) => $emit('toggleVisibility', id)"
            @move-up="(id) => $emit('moveUp', id)"
            @move-down="(id) => $emit('moveDown', id)"
            @select-configuration="(id) => $emit('selectConfiguration', id)"
            @import="$emit('import')"
            @export="$emit('export')"
            @export-all="$emit('exportAll')"
        />

        <!-- ═══ PARTIE 2 : Ligne d'actions Assign / Remove ═══ -->
        <!-- ✅ Wrapper englobant (étape 'actions') -->
        <GenesisTutorialItem :id="sub('actions')">
            <div 
                class="flex"
                :class="viewMode === 'two-view' ? 'flex-col gap-4 w-full' : 'items-center justify-start gap-3'"
            >
                <!-- ✅ Bouton Assign All -->
                <GenesisTutorialItem :id="sub('assignAll')">
                    <GenesisButton
                        :variant="'accent'"
                        :size="viewMode === 'two-view' ? 'lg' : 'sm'"
                        :disabled="!selectedConfigId || !canAssignAll"
                        @click="handleAssignAll"
                        :justify-between="true"
                        title="Assigner toutes les entités visibles à cette configuration"
                        is-perspective
                    >
                        <template v-if="viewMode === 'two-view'" #rightIcon>
                            <IconChevronsRight />
                        </template>
                        Assign All
                    </GenesisButton>
                </GenesisTutorialItem>

                <!-- ✅ Bouton Assign -->
                <GenesisTutorialItem :id="sub('assign')">
                    <GenesisButton
                        variant="secondary"
                        :size="viewMode === 'two-view' ? 'lg' : 'sm'"
                        :disabled="!selectedConfigId || !canAssign"
                        @click="handleAssign"
                        :justify-between="true"
                        title="Assigner les éléments sélectionnés à cette configuration"
                        is-perspective
                    >
                        <template v-if="viewMode === 'two-view'" #rightIcon>
                            <IconChevronRight />
                        </template>
                        Assign
                    </GenesisButton>
                </GenesisTutorialItem>

                <!-- ✅ Bouton Remove -->
                <GenesisTutorialItem :id="sub('remove')">
                    <GenesisButton
                        :size="viewMode === 'two-view' ? 'lg' : 'sm'"
                        :disabled="!selectedConfigId || !canRemove"
                        @click="handleRemove"
                        :justify-between="true"
                        title="Retirer les éléments sélectionnés de cette configuration"
                        is-perspective
                    >
                        <template v-if="viewMode === 'two-view'" #leftIcon>
                            <IconChevronLeft />
                        </template>
                        Remove
                    </GenesisButton>
                </GenesisTutorialItem>

                <!-- ✅ Bouton Remove All -->
                <GenesisTutorialItem :id="sub('removeAll')">
                    <GenesisButton
                        variant="neutral"
                        :size="viewMode === 'two-view' ? 'lg' : 'sm'"
                        :disabled="!selectedConfigId || !canRemoveAll"
                        @click="handleRemoveAll"
                        :justify-between="true"
                        title="Retirer toutes les entités visibles de cette configuration"
                        is-perspective
                    >
                        <template v-if="viewMode === 'two-view'" #leftIcon>
                            <IconChevronsLeft />
                        </template>
                        Remove All
                    </GenesisButton>
                </GenesisTutorialItem>

            </div>
        </GenesisTutorialItem>

    </div>
</template>

<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
// import GenesisConfigurationPanel from './GenesisConfigurationPanel.vue';
import GenesisConfigurationManageData from './GenesisConfigurationManageData.vue';
import type { ConfigurationItem } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import IconChevronLeft from '../../../ui/icons/IconChevronLeft.vue';
import IconChevronRight from '../../../ui/icons/IconChevronRight.vue';
import IconChevronsLeft from '../../../ui/icons/IconChevronsLeft.vue';
import IconChevronsRight from '../../../ui/icons/IconChevronsRight.vue';

// ✅ Tutoriel
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import { useTutorialGroup } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

const props = withDefaults(defineProps<{
    configurations: ConfigurationItem[];
    selectedConfigId: string | number | null;
    filteredConfigs: ConfigurationItem[];
    searchQuery: string;
    canMoveUp: boolean;
    canMoveDown: boolean;
    selectedItems: string[];
    viewMode?: 'one-view' | 'two-view';
    canAssign?: boolean;
    canRemove?: boolean;
    canAssignAll?: boolean;
    canRemoveAll?: boolean;
    showToggleVisibility?: boolean;

    // ✅ Ancrages tutoriel (deux canaux distincts)
    /** Ancrage pour GenesisConfigurationPanel (sous-étapes search/list/actions) */
    panelTutorialId?: string;
    /** Ancrage pour les boutons Assign / Remove / Assign All / Remove All */
    actionsTutorialId?: string;
    actionsDataTutorialId?: string;
    /** Active/désactive tous les sous-tutoriels du selector */
    includeSubTutorial?: boolean;

    // ✅ Textes personnalisables (fallback par défaut ci-dessous)
    tutorialTexts?: Partial<Record<'actions' | 'assignAll' | 'assign' | 'remove' | 'removeAll', string>>;
}>(), {
    viewMode: 'one-view',
    canAssignAll: true,
    canRemoveAll: true,
    canAssign: true,
    canRemove: true,
    showToggleVisibility: false,
    includeSubTutorial: false,
});

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
    assign: [configId: string | number, items: string[]];
    remove: [configId: string | number, items: string[]];
    assignAll: [configId: string | number];
    removeAll: [configId: string | number];
    import: [];
    export: [];
    exportAll: [];
}>();

// ═══ HANDLERS D'ASSOCIATION ═══

function handleAssign() {
    if (props.selectedConfigId !== null && props.selectedItems.length > 0) {
        emit('assign', props.selectedConfigId, props.selectedItems);
    }
}

function handleRemove() {
    if (props.selectedConfigId !== null && props.selectedItems.length > 0) {
        emit('remove', props.selectedConfigId, props.selectedItems);
    }
}

function handleAssignAll() {
    if (props.selectedConfigId !== null) {
        emit('assignAll', props.selectedConfigId);
    }
}

function handleRemoveAll() {
    if (props.selectedConfigId !== null) {
        emit('removeAll', props.selectedConfigId);
    }
}

// ═══ TUTORIEL : sous-étapes des actions ═══

const DEFAULT_ACTION_STEPS = {
    actions:    'Actions disponibles pour assigner ou retirer des entités au profil sélectionné.',
    assignAll:  'Assigne toutes les entités visibles au profil sélectionné.',
    assign:     'Assigne uniquement les entités sélectionnées au profil.',
    remove:     'Retire les entités sélectionnées du profil actif.',
    removeAll:  'Retire toutes les entités visibles du profil actif.',
} as const;

const { sub } = useTutorialGroup({
    anchorId: () => props.actionsTutorialId,
    include: () => props.includeSubTutorial,
    steps: [
        { key: 'actions',    text: props.tutorialTexts?.actions    ?? DEFAULT_ACTION_STEPS.actions    },
        { key: 'assignAll',  text: props.tutorialTexts?.assignAll  ?? DEFAULT_ACTION_STEPS.assignAll  },
        { key: 'assign',     text: props.tutorialTexts?.assign     ?? DEFAULT_ACTION_STEPS.assign     },
        { key: 'remove',     text: props.tutorialTexts?.remove     ?? DEFAULT_ACTION_STEPS.remove     },
        { key: 'removeAll',  text: props.tutorialTexts?.removeAll  ?? DEFAULT_ACTION_STEPS.removeAll  },
    ],
});
</script>