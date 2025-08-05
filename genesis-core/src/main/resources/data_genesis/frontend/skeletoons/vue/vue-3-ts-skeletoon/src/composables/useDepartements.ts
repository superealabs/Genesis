// src/composables/useDepartements.ts
import { ref, onMounted } from "vue";
import type { Departements } from "../models/DepartementsModel";
import * as departementService from "@/services/DepartementsService";

export function useDepartements(autoLoad = true) {
  const departements = ref<Departements[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const loadDepartements = async () => {
    loading.value = true;
    const { data, error: err } = await departementService.getAll();
    if (err) {
      console.log("error values");

      error.value = err;
    } else {
      console.log("fetched values");

      departements.value = data;
    }
    loading.value = false;
  };

  if (autoLoad) onMounted(loadDepartements);

  return {
    departements,
    loading,
    error,
    refresh: loadDepartements,
  };
}
