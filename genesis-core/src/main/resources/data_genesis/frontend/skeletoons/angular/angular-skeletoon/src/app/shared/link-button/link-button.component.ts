import { Component, Input } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';

@Component({
  selector: 'app-link-button',
  standalone: true,
  imports: [RouterModule, NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault],
  template: `
    <ng-container *ngIf="links">
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
    }
    .link-btn:hover {
      background-color: #194a9c;
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

  get linkKeys(): string[] {
    return Object.keys(this.links);
  }
}
