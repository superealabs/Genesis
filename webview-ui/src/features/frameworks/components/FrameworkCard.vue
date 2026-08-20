<template>
    <GenesisCard
        :label="framework.name"
        :sublabel="framework.coreFramework"
        :selected="selected"
        :showInfoButton="true"
        @click="$emit('select', framework)"
        @info="$emit('info', framework)"
    >
        <!-- TODO: slot logo quand les SVG seront disponibles -->
    </GenesisCard>
</template>

<script setup lang="ts">
import type { Framework } from '../types/framework.types';
import GenesisCard from '@/core/components/layouts/display/GenesisCard.vue';

defineProps<{
    framework: Framework;
    selected?: boolean;
}>();

defineEmits<{
    select: [framework: Framework];
    info: [framework: Framework];
}>();
</script>