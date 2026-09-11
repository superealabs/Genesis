import type { ComputedRef } from 'vue';

export const GENESIS_LIST_CONTEXT = Symbol('GenesisListContext');

export interface GenesisListContext {
    display: ComputedRef<'grid' | 'list'>;
    deletable: ComputedRef<boolean>;
}