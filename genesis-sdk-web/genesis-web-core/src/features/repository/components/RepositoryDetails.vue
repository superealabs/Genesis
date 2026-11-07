<!-- genesis-sdk-web/genesis-web-core/src/features/repository/components/RepositoryDetails.vue -->
<template>
    <GenesisDetails :title="provider.name">
        <template #logo>
            <GenesisLogo :src="resolveRepositoryLogo(provider) ?? ''" :size="'xl'" />
        </template>

        <!-- La responsivité dépend de la largeur de ce conteneur, pas de l'écran -->
        <div class="flex flex-col gap-y-12 px-4 @md:px-12 @container">

            <!-- ═══ GROUPE 1 : IDENTIFICATION ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">
                    Identification
                </h3>

                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in identityRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                        <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                        <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
                    </div>
                </dl>
            </section>

            <!-- ═══ GROUPE 2 : AUTHENTIFICATION ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">
                    Authentification
                </h3>

                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in authRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                        <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                        <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
                    </div>
                </dl>
            </section>

            <!-- ═══ GROUPE 3 : NAMESPACE & HÉBERGEMENT ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">
                    Namespace & Hébergement
                </h3>

                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in namespaceRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                        <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                        <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
                    </div>
                </dl>
            </section>

        </div>
    </GenesisDetails>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';
import GenesisLogo from '@genesis-labs/web-core/core/components/ui/logo/GenesisLogo.vue';
import { resolveRepositoryLogo } from '../assets/repositoryLogos'; // 🔧 À adapter au chemin réel

const props = defineProps<{ provider: RepositoryProviderMetadata }>();

const yesNo = (value: boolean | undefined): string => (value ? 'Oui' : 'Non');

const orFallback = (value: string | number | null | undefined, fallback = 'Non spécifié'): string =>
    value !== null && value !== undefined && value !== '' ? String(value) : fallback;

// Formatage lisible des méthodes d'authentification
const authMethodsLabel = computed(() => {
    const methods = props.provider.supportedAuthMethods;
    if (!methods || methods.length === 0) return 'Aucune';

    return methods.map(m => {
        if (m === 'personal_access_token') return 'Token (PAT / App Password)';
        if (m === 'ssh_key') return 'Clé SSH';
        if (m === 'username_password') return 'Utilisateur / Mot de passe';
        return m;
    }).join(', ');
});

// Support self-hosted : logique indicative (à affiner si le type le permet)
const supportsSelfHosted = computed(() => {
    const url = props.provider.defaultBaseUrl;
    return url === '' || url.includes('custom');
});

// Groupe 1 : ce qui identifie le provider
const identityRows = computed(() => {
    const p = props.provider;
    return [
        { label: 'Fournisseur',     value: orFallback(p.name) },
        { label: 'URL par défaut',  value: orFallback(p.defaultBaseUrl, '—') },
    ];
});

// Groupe 2 : authentification
const authRows = computed(() => {
    return [
        { label: 'Méthodes supportées', value: authMethodsLabel.value },
    ];
});

// Groupe 3 : namespace et hébergement
const namespaceRows = computed(() => {
    const p = props.provider;
    return [
        { label: 'Namespace requis',        value: yesNo(p.requiresNamespace) },
        { label: 'Libellé du namespace',    value: orFallback(p.namespaceLabel, '—') },
        { label: 'Self-hosted supporté',    value: yesNo(supportsSelfHosted.value) },
    ];
});
</script>