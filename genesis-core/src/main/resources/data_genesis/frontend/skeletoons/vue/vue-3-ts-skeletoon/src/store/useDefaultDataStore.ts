// stores/useDepartementsStore.ts
import { defineStore } from "pinia";

export const useDefaultDataStore = defineStore("defaulData", {
  state: () => ({
    pagination: {
      size: 10,
      itemsPerPageOptions: [
        { label: "2", value: 2 },
        { label: "10", value: 10 },
        { label: "20", value: 20 },
        { label: "30", value: 30 },
      ],
    },
  }),
  actions: {},
});
