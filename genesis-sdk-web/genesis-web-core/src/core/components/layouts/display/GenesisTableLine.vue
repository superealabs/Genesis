<template>
    <tr
        class="table-row transition-colors group cursor-pointer"
        :class="containerClasses"
        @click="$emit('click', $event)"
    >
        <!-- Le parent fournit ses <td> via #default -->
        <slot />

        <!-- Wrapper unifié pour le badge et les actions -->
        <td
            v-if="badge || deletable || showInfoButton"
            class="p-3 text-center relative"
        >
            <!-- Badge flottant en haut à droite de cette cellule -->
            <div v-if="badge" :class="badgeClasses + ' right-1 top-1 absolute z-20'">
                {{ badge }}
            </div>

            <!-- Conteneur des actions -->
            <div class="flex items-center justify-center gap-1">
                <GenesisButtonIcon
                    v-if="showInfoButton"
                    size="lg"
                    variant="tertiary"
                    :hover-himself="true"
                    @click.stop="$emit('info')"
                >
                    <IconHelpCircle />
                </GenesisButtonIcon>
                <GenesisButtonIcon
                    v-if="deletable"
                    size="lg"
                    variant="tertiary"
                    @click.stop="$emit('close')"
                >
                    <IconTrashAlt />
                </GenesisButtonIcon>
            </div>
        </td>
    </tr>
</template>

<script setup lang="ts">
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelpCircle from '@genesis-labs/web-core/core/components/ui/icons/IconHelpCircle.vue';
import IconTrashAlt from '../../ui/icons/IconTrashAlt.vue';

defineProps<{
    containerClasses: string;
    badgeClasses: string;
    badge: string | null;
    showInfoButton: boolean;
    deletable: boolean;
}>();

defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();
</script>