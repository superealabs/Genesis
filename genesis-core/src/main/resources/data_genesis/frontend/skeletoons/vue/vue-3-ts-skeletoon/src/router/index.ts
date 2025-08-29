import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import CustomisationView from '@/views/settings/CustomisationView.vue'

const routes: Array<RouteRecordRaw> = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/customisation', name: 'customisation', component: CustomisationView },
  {
    path: '/departements',
    name: 'departementlistview',
    component: () => import('@/views/departement/DepartementListView.vue'),
  },
  {
    path: '/departements/:id',
    name: 'departementdetailsview',
    component: () => import('@/views/departement/DepartementDetailsView.vue'),
  },
  {
    path: '/departements/update/:id',
    name: 'departementupdateview',
    component: () => import('@/views/departement/DepartementUpdateView.vue'),
  },
  {
    path: '/departements/create',
    name: 'departementcreateview',
    component: () => import('@/views/departement/DepartementCreateView.vue'),
  },
  {
    path: '/employes',
    name: 'employelistview',
    component: () => import('@/views/employe/EmployeListView.vue'),
  },
  {
    path: '/employes/:id',
    name: 'employedetailsview',
    component: () => import('@/views/employe/EmployeDetailsView.vue'),
  },
  {
    path: '/employes/update/:id',
    name: 'employeupdateview',
    component: () => import('@/views/employe/EmployeUpdateView.vue'),
  },
  {
    path: '/employes/create',
    name: 'employecreateview',
    component: () => import('@/views/employe/EmployeCreateView.vue'),
  },
  {
    path: '/projets',
    name: 'projetlistview',
    component: () => import('@/views/projet/ProjetListView.vue'),
  },
  {
    path: '/projets/:id',
    name: 'projetdetailsview',
    component: () => import('@/views/projet/ProjetDetailsView.vue'),
  },
  {
    path: '/projets/update/:id',
    name: 'projetupdateview',
    component: () => import('@/views/projet/ProjetUpdateView.vue'),
  },
  {
    path: '/projets/create',
    name: 'projetcreateview',
    component: () => import('@/views/projet/ProjetCreateView.vue'),
  },
  {
    path: '/taches',
    name: 'tachelistview',
    component: () => import('@/views/tache/TacheListView.vue'),
  },
  {
    path: '/taches/:id',
    name: 'tachedetailsview',
    component: () => import('@/views/tache/TacheDetailsView.vue'),
  },
  {
    path: '/taches/update/:id',
    name: 'tacheupdateview',
    component: () => import('@/views/tache/TacheUpdateView.vue'),
  },
  {
    path: '/taches/create',
    name: 'tachecreateview',
    component: () => import('@/views/tache/TacheCreateView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
