<template>
    <GenesisDetails :title="provider.name">
        <dl class="grid grid-cols-2 gap-x-8 gap-y-5">
            <div v-for="row in rows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
            </div>
        </dl>
    </GenesisDetails>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';

const props = defineProps<{ provider: RepositoryProviderMetadata }>();

const yesNo = (value: boolean) => (value ? 'Oui' : 'Non');

// Formatage lisible des méthodes d'authentification
const authMethodsLabel = computed(() => {
    return props.provider.supportedAuthMethods.map(m => {
        if (m === 'personal_access_token') return 'Token (PAT / App Password)';
        if (m === 'ssh_key') return 'Clé SSH';
        if (m === 'username_password') return 'Utilisateur / Mot de passe';
        return m;
    }).join(', ');
});

const rows = computed(() => {
    const p = props.provider;
    return [
        { label: 'Fournisseur', value: p.name },
        { label: 'URL par défaut', value: p.defaultBaseUrl },
        { label: 'Authentifications', value: authMethodsLabel.value },
        { label: 'Espace de nommage requis', value: yesNo(p.requiresNamespace) },
        { label: 'Libellé de l\'espace', value: p.namespaceLabel },
        { label: 'Supporte le self-hosted', value: yesNo(p.defaultBaseUrl === '' || p.defaultBaseUrl.includes('custom')) } // Logique indicative
    ];
});
</script>