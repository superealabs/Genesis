import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";

import "./assets/styles/scss/genesis-style.scss";
import "./assets/styles/scss/vendor.scss";
import "@fortawesome/fontawesome-free/css/all.min.css";

createApp(App).use(router).mount("#app");
