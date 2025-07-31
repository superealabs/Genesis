import { ref, toRaw } from "vue";

export function useSearch<T extends Record<string, any>>(
  model: T,
  emit: (event: "search", payload: Record<string, any>) => void
) {
  const filter = ref<Record<string, any>>({});
  const filterTypes = ref<Record<string, string>>({});

  const modelRaw = toRaw(model);

  Object.entries(modelRaw).forEach(([key, value]) => {
    filter.value[key] = "";
    const valueType = typeof value;
    if (valueType === "number") {
      filterTypes.value[key] = "number";
    } else if (value instanceof Date) {
      filterTypes.value[key] = "date";
    } else {
      filterTypes.value[key] = "text";
    }
  });

  const emitSearch = () => {
    emit("search", { ...filter.value });
  };

  const resetFilters = () => {
    Object.keys(filter.value).forEach((key) => {
      filter.value[key] = "";
    });
    emitSearch();
  };

  return {
    filter,
    filterTypes,
    emitSearch,
    resetFilters,
  };
}
