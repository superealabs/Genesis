import { toRaw } from "vue";

export function useSearch<T extends Record<string, any>>(
  model: T,
  emit: (event: "search", payload: Record<string, any>) => void
) {
  const emitSearch = () => {
    const rawModel = toRaw(model);
    const filtered: Record<string, any> = {};

    for (const key in rawModel) {
      const value = rawModel[key];
      if (
        value !== null &&
        value !== undefined &&
        value !== "" &&
        !(typeof value === "string" && value.trim() === "")
      ) {
        filtered[key] = value;
      }
    }

    emit("search", filtered);
  };

  return {
    emitSearch,
  };
}
