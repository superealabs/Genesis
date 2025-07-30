import { Component, Input } from '@angular/core';
import { LinkButtonComponent } from '../link-button/link-button.component'; // adapte le chemin si besoin
import { NgIf } from '@angular/common';
@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [LinkButtonComponent, NgIf],
  template: `
    <div class="page-header">
      <div class="page-title">
        <span class="light-name">{{ name }}</span> <span class="light-name"> / </span> <span class="bold-list">List</span>
      </div>
      <app-link-button [links]="linkMap" type="add"></app-link-button>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin: 1.5rem 0 1rem;
    }

    .page-title {
      /* plus de font-weight ici */
    }

    .light-name, .bold-list {
      font-size: 1.1rem; /* même taille pour les deux */
    }

    .light-name {
      font-weight: 700;
      font-size: 1.4rem;
    }

    .bold-list {
      font-weight: 700; /* plus bold */
      color: #6b7280; /* couleur subtile */
      font-size: 1.4rem;
    }
  `]
})
export class PageHeaderComponent {
  @Input() name: string = '';

  get singularLowercase(): string {
    return this.name.endsWith('s') ?
      this.name.slice(0, -1).toLowerCase() :
      this.name.toLowerCase();
  }

  get linkMap(): Record<string, string> {
    return {
      ['Add new ' + this.singularLowercase]: `/${this.name.toLowerCase()}/add`
    };
  }
}
