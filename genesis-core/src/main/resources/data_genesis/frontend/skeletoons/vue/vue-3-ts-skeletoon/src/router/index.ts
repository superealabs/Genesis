import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import DepartementListView from "@/views/departement/list/DepartementListView.vue";
import EmployeListView from "@/views/employe/list/EmployeListView.vue";
import ProjetListView from "@/views/projet/list/ProjetListView.vue";
import TacheListView from "@/views/tache/list/TacheListView.vue";
import TacheDetaiilsView from "@/views/tache/TacheDetaiilsView.vue";
import TacheCreateView from "@/views/tache/TacheCreateView.vue";
import TacheUpdateView from "@/views/tache/TacheUpdateView.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/departements",
    name: "departementlistview",
    component: DepartementListView,
  },
  {
    path: "/employes",
    name: "employelistview",
    component: EmployeListView,
  },
  {
    path: "/projets",
    name: "projetlistview",
    component: ProjetListView,
  },
  {
    path: "/taches",
    name: "tachelistview",
    component: TacheListView,
  },
  {
    path: "/taches/:id",
    name: "tachedetailsview",
    component: TacheDetaiilsView,
  },
  {
    path: "/taches/create",
    name: "tachecreateview",
    component: TacheCreateView,
  },
  {
    path: "/taches/update/:id",
    name: "tacheupdateview",
    component: TacheUpdateView,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
