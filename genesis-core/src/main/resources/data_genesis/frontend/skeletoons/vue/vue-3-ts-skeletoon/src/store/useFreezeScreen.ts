// stores/useDepartementsStore.ts
import { defineStore } from "pinia";

export const useFreezeScreen = defineStore("freezeScreen", {
  state: () => ({
    freezeState: false,
    freezeMessage: "",
  }),
  actions: {
    freeze(msg: string) {
      console.log("freeze " + msg);
      this.freezeMessage = msg;
      this.freezeState = true;
    },
    unfreeze() {
      this.freezeState = false;
    },
    debug(location: string) {
      console.log(
        `[DEBUG ${location}] freezeState = ${this.freezeState}, freezeMessage = "${this.freezeMessage}"`
      );
    },
  },
});
