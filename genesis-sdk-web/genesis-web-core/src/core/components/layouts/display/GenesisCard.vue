<template>
    <div
        class="relative rounded-lg cursor-pointer transition-all duration-200"
        :class="[layoutClasses, containerClasses]"
        @click="$emit('click', $event)"
    >
        <!-- Badge + close -->
        <div v-if="badge || deletable" class="absolute top-2 right-2 flex items-center gap-1 z-10">
            <GenesisButtonIcon
                v-if="deletable"
                size="xs"
                variant="tertiary"
                class="opacity-60 hover:opacity-100"
                @click.stop="$emit('close')"
            >
                <IconTrashAlt />
            </GenesisButtonIcon>
            <div v-if="badge" :class="badgeClasses">
                {{ badge }}
            </div>
        </div>

        <!-- Logo -->
        <div v-if="showLogo" :class="logoClasses">
            <slot name="logo">{{ initials }}</slot>
        </div>

        <!-- Zone texte : #header custom ou label/sublabel -->
        <div :class="textClasses">
            <template v-if="hasHeaderSlot">
                <slot name="header" />
            </template>
            <template v-else>
                <span v-if="label" :class="labelClasses">{{ label }}</span>
                <span v-if="sublabel" :class="sublabelClasses">{{ sublabel }}</span>
            </template>
        </div>

        <!-- Contenu complémentaire -->
        <div v-if="showComplementary && hasComplementary" :class="complementaryClasses">
            <slot name="complementary" />
        </div>

        <!-- Bouton info -->
        <GenesisButtonIcon
            v-if="showInfoButton"
            size="xs"
            variant="tertiary"
            :class="infoButtonClasses"
            :hover-himself="true"
            @click.stop="$emit('info')"
        >
            <IconHelpCircle />
        </GenesisButtonIcon>
    </div>
</template>

<script setup lang="ts">
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelpCircle from '@genesis-labs/web-core/core/components/ui/icons/IconHelpCircle.vue';
import IconTrashAlt from '../../ui/icons/IconTrashAlt.vue';

defineProps<{
    containerClasses: string;
    layoutClasses: string;
    badgeClasses: string;
    logoClasses: string;
    textClasses: string;
    labelClasses: string;
    sublabelClasses: string;
    complementaryClasses: string;
    infoButtonClasses: string;
    label?: string;
    sublabel?: string;
    badge: string | null;
    showInfoButton: boolean;
    showComplementary: boolean;
    showLogo: boolean;
    deletable: boolean;
    initials: string;
    hasHeaderSlot: boolean;
    hasComplementary: boolean;
}>();

defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();
</script>