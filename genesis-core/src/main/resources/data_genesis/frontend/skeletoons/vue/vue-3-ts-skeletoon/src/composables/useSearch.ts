import { ref, computed, onMounted } from "vue";
import { EntitySearchField } from "@/models/EntityModel";

export function useSearch(
  initialModel: Record<string, any>,
  availableFilters: EntitySearchField[],
  emit: (event: "search", payload: Record<string, any>) => void
) {
  const searchModel = ref({ ...initialModel });
  const activeFieldKeys = ref<string[]>([]);
  const selectedFieldToAdd = ref<string>("");

  // Holds the actual field metadata (mutated with options if loaded)
  const fields = ref<EntitySearchField[]>([...availableFilters]);

  // Load async options for fields with optionsLoader
  async function loadOptions() {
    for (const field of fields.value) {
      if (field.type === "select" && field.optionsLoader) {
        try {
          field.options = await field.optionsLoader();
        } catch (err) {
          console.error(`Failed to load options for ${field.key}:`, err);
          field.options = [];
        }
      }
    }
  }

  const availableFields = computed(() =>
    fields.value.filter((f) => !activeFieldKeys.value.includes(f.key))
  );

  const activeFields = computed(() =>
    fields.value.filter((f) => activeFieldKeys.value.includes(f.key))
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
      const value = searchModel.value[key];
      if (value === undefined || value === null || value === "") continue;

      const fieldMeta = fields.value.find((f) => f.key === key);

      if (fieldMeta?.type === "select" && fieldMeta.searchKey) {
        // Wrap select values into nested object for API
        filtered[key] = { [fieldMeta.searchKey]: value };
      } else {
        filtered[key] = value;
      }
    }

    return filtered;
  }

  onMounted(() => {
    loadOptions();
  });

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
    fields, // expose fields with loaded options
  };
}
