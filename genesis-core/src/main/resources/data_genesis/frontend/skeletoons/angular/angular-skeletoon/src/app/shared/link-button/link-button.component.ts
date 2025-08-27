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
          <i *ngSwitchCase="'back'" class="bi bi-arrow-left"></i>
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
          <i *ngSwitchCase="'back'" class="bi bi-arrow-left"></i>
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
  justify-content: center;
  margin: 0.5rem;
  padding: 0.6rem 1.5rem;
  font-size: 1rem;
  font-weight: 500;
  background: linear-gradient(145deg, #2263cb, #3a7de0); 
  color: white;
  border-radius: 0.35rem;
  text-decoration: none;
  text-transform: lowercase;
  transition: background 0.3s ease, box-shadow 0.3s ease;
  gap: 6px;
  outline: none;
  border: none;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.link-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -50%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0.4) 0%,
    rgba(255, 255, 255, 0.1) 60%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
}

.link-btn:hover {
  background: linear-gradient(145deg, #194a9c, #2263cb);
  box-shadow: 0 6px 12px rgba(0,0,0,0.2);
}

  .link-btn:focus {
    outline: none;          
    box-shadow: none;       
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
