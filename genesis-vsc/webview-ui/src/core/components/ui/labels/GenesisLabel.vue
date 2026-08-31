<template>
    <div class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-accent/10 text-accent text-xs font-medium border border-accent/20 transition-colors group">
        <!-- Texte du label avec troncature si trop long -->
        <span class="truncate max-w-[150px]">
            {{ text }}
        </span>
        
        <!-- Bouton de fermeture -->
        <GenesisButtonIcon
            v-if="removable"
            variant="tertiary"
            size="md"
            hover-himself
            @click="$emit('remove')"
            class="!w-4 !h-4 [&_svg]:!w-3 [&_svg]:!h-3 opacity-70 hover:opacity-100"
            title="Supprimer"
        >
            <!-- Icône X (Cross) -->
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
        </GenesisButtonIcon>
    </div>
</template>

<script setup lang="ts">
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';

interface Props {
    /** Le texte à afficher dans le label */
    text: string;
    /** Affiche ou masque le bouton de suppression */
    removable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    removable: true
});

defineEmits<{
    /** Émis lorsque l'utilisateur clique sur le bouton de suppression */
    remove: [];
}>();

defineOptions({ 
    inheritAttrs: false 
});
</script>