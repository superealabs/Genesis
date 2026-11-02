import { computed, ref, toValue, type MaybeRefOrGetter } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

type TriState = 'all' | 'yes' | 'no';
type SearchTarget = 'all' | 'parent' | 'child';

const NEXT_TRI_STATE: Record<TriState, TriState> = { all: 'yes', yes: 'no', no: 'all' };

/**
 * Recherche + filtres (cible de recherche, Formulaire, Obligatoire) d'un tableau de relations.
 * Partagé par RelationTableView et RelationTableViewEditable.
 */
export function useRelationFilters(relations: MaybeRefOrGetter<RelationParameter[]>) {
  const searchQuery = ref('');
  const filterForm = ref<TriState>('all');
  const filterMandatory = ref<TriState>('all');
  const searchTarget = ref<SearchTarget>('all');

  const searchPlaceholder = computed(() => {
    if (searchTarget.value === 'parent') return 'Rechercher dans les tables parentes...';
    if (searchTarget.value === 'child') return 'Rechercher dans les tables filles...';
    return 'Rechercher une table (Parent ou Child)...';
  });

  const toggleFilterForm = () => { filterForm.value = NEXT_TRI_STATE[filterForm.value]; };
  const toggleFilterMandatory = () => { filterMandatory.value = NEXT_TRI_STATE[filterMandatory.value]; };
  const toggleSearchTarget = (target: Exclude<SearchTarget, 'all'>) => {
    searchTarget.value = searchTarget.value === target ? 'all' : target;
  };

  const filteredRelations = computed(() => {
    const query = searchQuery.value.toLowerCase().trim();

    return toValue(relations).filter(rel => {
      if (query) {
        const parent = rel.parentTable.toLowerCase().includes(query);
        const child = rel.childTable.toLowerCase().includes(query);
        const matches =
          searchTarget.value === 'parent' ? parent :
          searchTarget.value === 'child' ? child :
          parent || child;
        if (!matches) return false;
      }
      if (filterForm.value !== 'all' && rel.hasForm !== (filterForm.value === 'yes')) return false;
      if (filterMandatory.value !== 'all' && rel.mandatory !== (filterMandatory.value === 'yes')) return false;
      return true;
    });
  });

  return {
    searchQuery,
    filterForm,
    filterMandatory,
    searchTarget,
    searchPlaceholder,
    filteredRelations,
    toggleFilterForm,
    toggleFilterMandatory,
    toggleSearchTarget,
  };
}