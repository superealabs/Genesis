import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';

@Component({
  selector: 'app-link-button',
  standalone: true,
  imports: [RouterModule, NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault],
  template: `
    <ng-container *ngIf="!isButton; else buttonTemplate">
      <a
        *ngFor="let key of linkKeys"
        [routerLink]="links[key]"
        class="link-btn"
        [attr.data-type]="type"
      >
        <ng-container [ngSwitch]="type">
          <i *ngSwitchCase="'search'" class="bi bi-search"></i>
          <i *ngSwitchCase="'add'" class="bi bi-plus-circle"></i>
          <i *ngSwitchDefault></i>
        </ng-container>
        {{ key }}
      </a>
    </ng-container>

    <ng-template #buttonTemplate>
      <button
        type="button"
        class="link-btn"
        [attr.data-type]="type"
        (click)="onClick.emit()"
      >
        <ng-container [ngSwitch]="type">
          <i *ngSwitchCase="'search'" class="bi bi-search"></i>
          <i *ngSwitchCase="'add'" class="bi bi-plus-circle"></i>
          <i *ngSwitchDefault></i>
        </ng-container>
        {{ buttonLabel }}
      </button>
    </ng-template>
  `,
 styles: [`
  @import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css');

  .link-btn {
    display: inline-flex;
    align-items: center;
    margin: 0.5rem;
    padding: 0.6rem 1.2rem;
    font-size: 1.0rem;
    background-color: #2263cb;
    color: white;
    border-radius: 0.35rem;
    text-decoration: none;
    text-transform: lowercase;
    transition: background-color 0.3s ease;
    gap: 6px;
    outline: none;
    border: none;           /* 🔹 supprime la bordure */
  }

  .link-btn:hover {
    background-color: #194a9c;
  }

  .link-btn:focus {
    outline: none;          /* 🔹 supprime le contour noir au focus clavier */
    box-shadow: none;       /* 🔹 supprime un éventuel halo */
  }

  .link-btn i {
    font-size: 1.1rem;
    vertical-align: middle;
  }
`]
})
export class LinkButtonComponent {
  @Input() links: Record<string, string> = {};
  @Input() type: string = 'default';

  @Input() isButton: boolean = false; // ✅ nouveau : mode bouton pur
  @Input() buttonLabel: string = 'Action'; // label du bouton
  @Output() onClick = new EventEmitter<void>(); // ✅ événement émis au clic

  get linkKeys(): string[] {
    return Object.keys(this.links);
  }
}
