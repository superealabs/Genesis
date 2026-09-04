<template>
    <!-- ═══ MODE LIST (tableau avec colonnes alignées) ═══ -->
    <div
        v-if="display === 'list'"
        class="w-full rounded-lg overflow-hidden bg-bg"
    >
        <table class="w-full text-sm text-left table-fixed">
            <!-- Header optionnel -->
            <thead
                v-if="showHeader && headers.length > 0"
                class="text-xs font-semibold text-muted uppercase tracking-wider bg-bg-light border-b border-secondary"
            >
                <tr>
                    <th
                        v-for="(header, index) in headers"
                        :key="index"
                        :class="header.class || 'p-3 text-center'"
                    >
                        {{ header.label }}
                    </th>
                    <th v-if="hasActionColumn" class="p-3 text-center">
                        Actions
                    </th>
                </tr>
            </thead>
            <tbody class="divide-y-2 divide-bg-dark">
                <slot />
            </tbody>
        </table>
    </div>

    <!-- ═══ MODE GRID ═══ -->
    <div v-else class="grid gap-3" :style="gridStyle">
        <slot />
    </div>
</template>

<script setup lang="ts">
import { computed, provide } from 'vue';
import { GENESIS_LIST_CONTEXT, type GenesisListContext } from './GenesisItem.types';

const props = withDefaults(defineProps<{
    display?: 'grid' | 'list';
    minColWidth?: string;
    headers?: { label: string; class?: string }[];
    showHeader?: boolean;
    haveActions?: boolean;
}>(), {
    display: 'grid',
    minColWidth: '120px',
    columnLayout: '1fr',
    headers: () => [],
    showHeader: true,
    haveActions: false,
});

provide<GenesisListContext>(GENESIS_LIST_CONTEXT, {
    display:      computed(() => props.display),
    deletable:     computed(() => props.haveActions),
});

const hasActionColumn = computed(() => props.haveActions);

const gridStyle = computed(() => {
    return `grid-template-columns: repeat(auto-fill, minmax(${props.minColWidth}, 1fr))`;
});
</script>