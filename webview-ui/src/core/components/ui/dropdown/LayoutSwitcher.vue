<template>
    <GenesisDropdown 
        :align="align" 
        :triggerSize="'lg'"
        :dropdownSize="'lg'"
        :openAtHover="true"
        :hideChevron="hideChevron"
    >
        <template #triggerIcon>
            <component :is="currentIcon" />
        </template>

        <GenesisButton
            :active="modelValue === 'grid'"
            @click="$emit('update:modelValue', 'grid')"
            size="lg"
            variant="secondary"
            :visibleBackground="false"
            :fillWidth="true"
        >
            <template #leftIcon>
                <IconGrid/>
            </template>
            Grid
        </GenesisButton>

        <GenesisButton
            :active="modelValue === 'list'"
            @click="$emit('update:modelValue', 'list')"
            size="lg"
            variant="secondary"
            :visibleBackground="false"
            :fillWidth="true"
        >
            <template #leftIcon>
                <IconListUl/>
            </template>
            List
        </GenesisButton>
    </GenesisDropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import IconGrid from '@/core/components/ui/icons/IconGrid.vue';
import IconListUl from '@/core/components/ui/icons/IconListUl.vue';

const props = withDefaults(defineProps<{
    modelValue: 'grid' | 'list';
    align?: 'left' | 'right';
    hideChevron?: boolean;
}>(), {
    align: 'right',
    hideChevron: false
});

defineEmits<{
    'update:modelValue': [value: 'grid' | 'list'];
}>();

const currentIcon = computed(() => {
    return props.modelValue === 'grid' ? IconGrid : IconListUl;
});
</script>