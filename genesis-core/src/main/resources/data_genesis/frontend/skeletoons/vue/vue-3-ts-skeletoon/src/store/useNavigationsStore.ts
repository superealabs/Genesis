import { defineStore } from "pinia";

export const useNavigationsStore = defineStore("navigations", {
  state: () => ({
    navigations: [
      // CORE NAVIGATION
      {
        icon: "bi bi-cpu",
        sectionName: "Core",
        navChilds: [
          {
            navLink: "/",
            navTitle: "Home",
          },
        ],
      },
      // ENTITITES NAVIGATION
      {
        icon: "bi bi-stack",
        sectionName: "Entities",
        navChilds: [
          {
            navTitle: "Departement",
            navLink: "",
            navChilds: [
              {
                navTitle: "List",
                navLink: "/departements",
              },
            ],
          },
          {
            navTitle: "Employe",
            navLink: "/employes",
          },
          {
            navTitle: "Projet",
            navLink: "/projets",
          },
          {
            navTitle: "Tache",
            navChilds: [
              {
                navTitle: "List",
                navLink: "/taches",
              },
              {
                navTitle: "Create",
                navLink: "/taches/create",
              },
            ],
          },
        ],
      },
      // VIEWS NAVIGATION
      {
        icon: "bi bi-view-stacked",
        sectionName: "Views",
        navChilds: [],
      },
      // SETTINGS NAVIGATION
      {
        icon: "bi bi-gear",
        sectionName: "Settings",
        navChilds: [
          {
            navTitle: "Customisation",
            navLink: "/custumisation",
          },
        ],
      },
    ],
  }),
  actions: {},
});
