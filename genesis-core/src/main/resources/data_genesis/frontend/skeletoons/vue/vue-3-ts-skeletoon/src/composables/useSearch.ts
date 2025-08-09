import { ref, computed } from "vue";
import { EntityListField } from "@/models/EntityModel";

export function useSearch(
  initialModel: Record<string, any>,
  availableFilters: EntityListField[],
  emit: (event: "search", payload: Record<string, any>) => void
) {
  const searchModel = ref({ ...initialModel });
  const activeFieldKeys = ref<string[]>([]);
  const selectedFieldToAdd = ref<string>("");

  const availableFields = computed(() =>
    availableFilters.filter((f) => !activeFieldKeys.value.includes(f.key))
  );

  const activeFields = computed(() =>
    availableFilters.filter((f) => activeFieldKeys.value.includes(f.key))
  );

  function activateField() {
    if (
      selectedFieldToAdd.value &&
      !activeFieldKeys.value.includes(selectedFieldToAdd.value)
    ) {
      activeFieldKeys.value.push(selectedFieldToAdd.value);
      selectedFieldToAdd.value = "";
    }
  }

  function desactivateField(key: string) {
    activeFieldKeys.value = activeFieldKeys.value.filter((k) => k !== key);
    searchModel.value[key] = undefined;
  }

  function resetFilters() {
    for (const key in searchModel.value) {
      searchModel.value[key] = undefined;
    }
    activeFieldKeys.value = [];
  }

  function getFiltersValues() {
    const filtered: Record<string, any> = {};
    for (const key of activeFieldKeys.value) {
      filtered[key] = searchModel.value[key];
    }
    return filtered;
  }

  return {
    searchModel,
    activeFieldKeys,
    selectedFieldToAdd,
    activeFields,
    availableFields,
    activateField,
    desactivateField,
    resetFilters,
    getFiltersValues,
  };
}
