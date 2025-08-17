<template>
  <div class="card border-0 shadow-sm">
    <div class="card-body">
      <form @submit.prevent="handleSubmit" class="row w-50 mx-auto g-3">
        <!-- Titre -->
        <div class="col-md-12">
          <GenesisInput
            label="Titre"
            placeholder="Entrer le titre"
            v-model="formModel.titre"
            type="text"
            required
          />
        </div>

        <!-- Description -->
        <div class="col-md-12">
          <GenesisInput
            label="Description"
            placeholder="Entrer la description"
            type="textarea"
            rows="3"
            v-model="formModel.description"
          />
        </div>

        <!-- Priorité -->
        <div class="col-md-12">
          <GenesisInput
            label="Priorité"
            placeholder="1 à 5"
            type="number"
            min="1"
            max="5"
            v-model="formModel.priorite"
          />
        </div>

        <!-- Projet -->
        <div class="col-md-12">
          <GenesisSelect
            label="Projet"
            :options="projetidProjetsData"
            v-model="formModel.projetidProjets"
          />
        </div>

        <!-- Assigné à -->
        <div class="col-md-12">
          <GenesisSelect
            label="Employé Assigné"
            :options="assigneaidEmployesData"
            v-model="formModel.assigneaidEmployes"
          />
        </div>

        <!-- Boutons -->
        <div class="row mt-4 justify-content-end g-3">
          <div class="col-auto">
            <GenesisButton
              type="submit"
              class="btn-primary text-white"
              icon="bi bi-save me-2"
              :label="submitLabel"
            />
          </div>
          <div class="col-auto">
            <GenesisButton
              @click="cancelForm"
              class="btn-secondary"
              label="Cancel"
            />
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from "vue";
import { TacheFormDTO } from "@/models/TacheModel";
import GenesisButton from "@/core/button/GenesisButton.vue";
import GenesisInput from "@/core/form/GenesisInput.vue";
import GenesisSelect from "@/core/form/GenesisSelect.vue";
import {
  extractSelectOptionsFromOjectsData,
  SelectOption,
} from "@/models/SelectOption";
import { useTaches } from "@/composables/useTaches";

export default defineComponent({
  name: "TacheForm",
  components: { GenesisButton, GenesisInput, GenesisSelect },
  props: {
    tache: { type: Object as PropType<TacheFormDTO>, required: false },
    submitLabel: { type: String, default: "Submit" },
  },
  emits: ["submit", "cancel"],
  setup(props, { emit }) {
    const formModel = ref<Partial<TacheFormDTO>>({ ...props.tache });
    const projetidProjetsData = ref<SelectOption[]>([]);
    const assigneaidEmployesData = ref<SelectOption[]>([]);
    const { loadFkMapData } = useTaches(false);

    async function loadFkData() {
      const { projets, employes } = await loadFkMapData();
      projetidProjetsData.value = extractSelectOptionsFromOjectsData(projets);
      assigneaidEmployesData.value =
        extractSelectOptionsFromOjectsData(employes);
    }

    onMounted(loadFkData);

    function handleSubmit() {
      emit("submit", formModel.value);
    }

    function cancelForm() {
      emit("cancel", formModel.value);
    }

    return {
      formModel,
      projetidProjetsData,
      assigneaidEmployesData,
      handleSubmit,
      cancelForm,
    };
  },
});
</script>
