import { defineStore } from 'pinia'
export const useNavigationsStore = defineStore('navigations', {
  state: () => ({
    navigations: [
      // CORE NAVIGATION
      {
        icon: 'fas fa-microchip',
        sectionName: 'Core',
        navChilds: [
          {
            navLink: '/',
            navTitle: 'Home',
          },
        ],
      },
      // ENTITIES NAVIGATION
      {
        icon: 'fas fa-layer-group',
        sectionName: 'Entities',
        navChilds: [
          {
            navTitle: 'List Departement',
            navLink: '/departements',
          },
          {
            navTitle: 'Create Departement',
            navLink: '/departements/create',
          },

          {
            navTitle: 'Employe',
            navLink: '/employes',
          },
          {
            navTitle: 'Create Employe',
            navLink: '/employes/create',
          },
          {
            navTitle: 'Projet',
            navLink: '/projets',
          },
          {
            navTitle: 'Create Projet',
            navLink: '/projets/create',
          },
          {
            navTitle: 'Tache',
            navLink: '/taches',
          },
          {
            navTitle: 'Create Tache',
            navLink: '/taches/create',
          },
        ],
      },
      // VIEWS NAVIGATION
      {
        icon: 'fas fa-box',
        sectionName: 'Views',
        navChilds: [],
      },
      // SETTINGS NAVIGATION
      {
        icon: 'fas fa-gear',
        sectionName: 'Settings',
        navChilds: [
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
          {
            navTitle: 'Customisation',
            navLink: '/customisation',
          },
        ],
      },
    ],
  }),
  actions: {},
})
