import { Component, Input } from '@angular/core';
import { LinkButtonComponent } from '../link-button/link-button.component';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [LinkButtonComponent, NgIf],
  template: `
    <div class="page-header">
      <div class="page-title">
        <span class="light-name">{{ name }}</span>
        <span class="light-name"> / </span>
        <span class="bold-list">List</span>
      </div>
      <app-link-button
        *ngIf="!isView"
        [links]="linkMap"
        type="add">
      </app-link-button>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin: 1.5rem 0 1rem;
    }

    .light-name, .bold-list {
      font-size: 1.1rem;
    }

    .light-name {
      font-weight: 700;
      font-size: 1.4rem;
    }

    .bold-list {
      font-weight: 700;
      color: #6b7280;
      font-size: 1.4rem;
    }
  `]
})
export class PageHeaderComponent {
  @Input() name: string = '';
  @Input() isView: boolean = false; // par défaut on affiche le bouton

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
