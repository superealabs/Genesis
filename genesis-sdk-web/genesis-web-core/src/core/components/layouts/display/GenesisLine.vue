<template>
    <!-- ═══ MODE LINE (Liste Flex, pas un tableau) ═══ -->
    <div
        class="flex items-center justify-between w-full p-3 rounded-lg cursor-pointer transition-colors group"
        :class="containerClasses"
        @click="$emit('click', $event)"
    >
        <!-- ═══ PARTIE GAUCHE : Logo + Textes ═══ -->
        <div class="flex items-center gap-3 flex-1 min-w-0">
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
        </div>

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
import IconTrashAlt from '../../ui/icons/IconTrashAlt.vue';

// On reçoit TOUTES les classes calculées par GenesisItem pour respecter le DRY
defineProps<{
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
}>();

defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();
</script>