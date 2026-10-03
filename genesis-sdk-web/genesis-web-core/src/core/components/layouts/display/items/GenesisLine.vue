<template>
    <!-- ═══ MODE LINE (Liste Flex, pas un tableau) ═══ -->
    <!-- `relative` : nécessaire pour ancrer la mascotte sur le bord haut -->
    <div
        class="relative flex items-center justify-between w-full p-3 rounded-lg cursor-pointer transition-colors group"
        :class="containerClasses"
        @click="$emit('click', $event)"
    >
        <!-- Mascotte -->
        <GenesisMascot :src="mascotSrc" :size="mascotSize" :selected="selected" />

        <!-- ═══ PARTIE GAUCHE : Logo + Textes (double couche pour l'effet de sélection) ═══ -->
        <GenesisSelectableIdentity
            class="flex-1 min-w-0"
            layer-class="flex items-center gap-3 min-w-0"
            :selected="selected"
        >
            <!-- Logo -->
            <div v-if="showLogo" :class="logoClasses">
                <slot name="logo">{{ initials }}</slot>
            </div>

            <!-- Zone de texte -->
            <div class="flex flex-col min-w-0">
                <template v-if="hasHeaderSlot">
                    <slot name="header" />
                </template>
                <template v-else>
                    <span v-if="label" :class="labelClasses">{{ label }}</span>
                    <span v-if="sublabel" :class="sublabelClasses">{{ sublabel }}</span>
                </template>
            </div>
        </GenesisSelectableIdentity>

        <!-- ═══ PARTIE DROITE : Complément + Badge + Actions ═══ -->
        <div class="flex items-center gap-3 flex-shrink-0">
            <!-- Contenu complémentaire (ex: Port, Tags) -->
            <div v-if="showComplementary && hasComplementary" :class="complementaryClasses">
                <slot name="complementary" />
            </div>

            <!-- Wrapper pour Badge et Actions -->
            <div class="flex items-center gap-2 relative">
                <!-- Badge -->
                <div v-if="badge" :class="badgeClasses">
                    {{ badge }}
                </div>

                <!-- Bouton Info -->
                <GenesisButtonIcon
                    v-if="showInfoButton"
                    size="lg"
                    variant="tertiary"
                    :hover-himself="true"
                    @click.stop="$emit('info')"
                >
                    <IconHelpCircle />
                </GenesisButtonIcon>

                <!-- Bouton Supprimer -->
                <GenesisButtonIcon
                    v-if="deletable"
                    size="lg"
                    variant="tertiary"
                    @click.stop="$emit('close')"
                >
                    <IconTrashAlt />
                </GenesisButtonIcon>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelpCircle from '@genesis-labs/web-core/core/components/ui/icons/IconHelpCircle.vue';
import IconTrashAlt from '../../../ui/icons/IconTrashAlt.vue';
import GenesisMascot from './GenesisMascot.vue';
import GenesisSelectableIdentity from './GenesisSelectableIdentity.vue';

// On reçoit TOUTES les classes, la mascotte et l'état de sélection calculés par GenesisItem (DRY)
withDefaults(defineProps<{
    containerClasses: string;
    badgeClasses: string;
    logoClasses: string;
    labelClasses: string;
    sublabelClasses: string;
    complementaryClasses: string;
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
    mascotSrc: string;
    mascotSize?: number;
    selected?: boolean;
}>(), {
    selected: false,
    mascotSize: 32
});

defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();
</script>