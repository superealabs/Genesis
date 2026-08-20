<template>
    <GenesisRow
        :label="framework.name"
        :sublabel="framework.coreFramework"
        :selected="selected"
        @click="$emit('select', framework)"
    >
        <!-- TODO: slot logo quand les SVG seront disponibles -->

        <template #right>
            <span
                class="text-xs px-2 py-0.5 rounded-full"
                :class="{
                    'bg-accent/20 text-accent': framework.type === 'REST_API',
                    'bg-secondary text-text-muted': framework.type === 'MVC'
                }"
            >
                {{ framework.type === 'REST_API' ? 'REST' : 'MVC' }}
            </span>
        </template>
    </GenesisRow>
</template>

<script setup lang="ts">
import type { Framework } from '../types/framework.types';
import GenesisRow from '@/core/components/layouts/display/GenesisRow.vue';

defineProps<{
    framework: Framework;
    selected?: boolean;
}>();

defineEmits<{
    select: [framework: Framework];
}>();
</script>