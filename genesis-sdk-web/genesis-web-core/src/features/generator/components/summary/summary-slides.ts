// features/generator/components/summary/summary-slides.ts
//
// Source de vérité unique des slides du résumé : ordre, libellé, composant.
// Pour ajouter / retirer / réordonner un slide, on ne touche qu'à ce tableau.

import type { Component } from 'vue';
import ProjectSummary from './ProjectSummary.vue';
import DatabaseSummary from './DatabaseSummary.vue';
import GenerationSummary from './GenerationSummary.vue';
import RelationSummary from './RelationSummary.vue';
import FrontendSummary from './FrontendSummary.vue';
import RepositorySummary from './RepositorySummary.vue';

export interface SummarySlide {
    id: string;
    label: string;       // affiché en infobulle sur la frise
    component: Component;
}

export const SUMMARY_SLIDES: SummarySlide[] = [
    { id: 'project',    label: 'Projet',          component: ProjectSummary },
    { id: 'database',   label: 'Base de données', component: DatabaseSummary },
    { id: 'generation', label: 'Génération',      component: GenerationSummary },
    { id: 'relations',  label: 'Relations',       component: RelationSummary },
    { id: 'frontend',   label: 'Frontend',        component: FrontendSummary },
    { id: 'repository', label: 'Repository',      component: RepositorySummary },
];