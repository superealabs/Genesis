import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import DepartementsListView from "@/views/departements/DepartementsListView.vue";
import EmployesListView from "@/views/employes/EmployesListView.vue";
import ProjetsListView from "@/views/projets/ProjetsListView.vue";
import TachesListView from "@/views/taches/TachesListView.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/departementss",
    name: "departementslistview",
    component: DepartementsListView,
  },
  {
    path: "/employess",
    name: "employeslistview",
    component: EmployesListView,
  },
  {
    path: "/projetss",
    name: "projetslistview",
    component: ProjetsListView,
  },
  {
    path: "/tachess",
    name: "tacheslistview",
    component: TachesListView,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
