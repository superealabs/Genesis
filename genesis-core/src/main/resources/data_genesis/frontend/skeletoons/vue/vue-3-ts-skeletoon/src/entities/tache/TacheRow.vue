<template>
  <tr class="border-bottom">
    <td>{{ tache.id }}</td>
    <td>{{ tache.titre }}</td>
    <td>{{ tache.description }}</td>
    <td>{{ tache.priorite }}</td>
    <td>{{ getSecondValue(tache.projetidProjets) }}</td>
    <td>{{ getSecondValue(tache.assigneaidEmployes) }}</td>

    <td class="text-end">
      <div class="d-inline-flex rounded-3 shadow-sm overflow-hidden">
        <EntityRowActions
          @delete-row="deleteRow"
          @view-row="viewRow"
          @edit-row="updateRow"
        />
      </div>
    </td>
  </tr>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { Tache } from "@/models/TacheModel";
import EntityRowActions from "@/core/table/EntityRowActions.vue";
import { usePopup } from "@/composables/usePopup";
import { useObjectUtils } from "@/composables/useObjectUtils";

export default defineComponent({
  name: "TacheRow",
  components: { EntityRowActions },
  props: {
    tache: {
      required: true,
      type: Object as PropType<Tache>,
    },
  },
  emits: ["request-delete", "request-view", "request-update"],
  setup(props, { emit }) {
    const { visible: deletePopup, closePopup, togglePopup } = usePopup(false);
    const deleteRow = () => {
      emit("request-delete", props.tache);
    };

    const viewRow = () => {
      emit("request-view", props.tache);
    };

    const updateRow = () => {
      emit("request-update", props.tache);
    };

    const { getSecondValue } = useObjectUtils();

    return {
      deletePopup,
      closePopup,
      togglePopup,
      deleteRow,
      viewRow,
      updateRow,
      getSecondValue,
    };
  },
});
</script>

<style scoped></style>
