import type { ComputedRef } from 'vue';

export const GENESIS_LIST_CONTEXT = Symbol('GenesisListContext');

export interface GenesisListContext {
    display: ComputedRef<'grid' | 'table' | 'list'>;
    deletable: ComputedRef<boolean>;
}