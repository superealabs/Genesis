import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LinkButtonComponent } from '../link-button/link-button.component';


export interface SearchField {
  key: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'select';
  options?: { value: any; label: any }[];
}

@Component({
  selector: 'app-dynamic-search-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LinkButtonComponent],
  template: `
    <form [formGroup]="searchForm" (ngSubmit)="onSearch()" class="search-form">

      <!-- Plus button -->
      <div *ngIf="hiddenFields.length > 0" class="form-group-inline btn-inline">
        <button type="button" class="btn-plus" (click)="toggleSelect()">＋</button>
        <select *ngIf="showSelect" (change)="onAddField($event)" class="form-control select-dropdown">
          <option value="">Select a field...</option>
          <option *ngFor="let field of hiddenFields" [value]="field.key">{{ field.label }}</option>
        </select>
      </div>

      <!-- Visible fields -->
      <div *ngFor="let field of visibleFields" class="form-group-inline">
        <div class="input-wrapper">
          <ng-container [ngSwitch]="field.type">
            <input *ngSwitchCase="'text'" [formControlName]="field.key" type="text" class="form-control" [placeholder]="field.label">
            <input *ngSwitchCase="'number'" [formControlName]="field.key" type="number" class="form-control" [placeholder]="field.label">
            <input *ngSwitchCase="'date'" [formControlName]="field.key" type="date" class="form-control" [placeholder]="field.label">
            <select *ngSwitchCase="'select'" [formControlName]="field.key" class="form-control">
              <option value="">{{ field.label }}</option>
              <option *ngFor="let option of field.options" [value]="option.value">{{ option.label }}</option>
            </select>
          </ng-container>
          <span class="remove-icon" (click)="removeField(field)">✖</span>
        </div>
      </div>

      <!-- Search button -->
      <div class="form-group-inline btn-inline">
        <app-link-button
          [isButton]="true"
          [buttonLabel]="'Search'"
          type="search"
          (onClick)="searchForm.valid && onSearch()"
          class="btn-search"
        ></app-link-button>
      </div>
    </form>
  `,
  styles: [`
    .search-form {
      display: flex;
      align-items: center;
      gap: 15px;
      flex-wrap: wrap; /* Permet le retour à la ligne */
      justify-content: flex-start; /* Alignement à gauche */
    }

    .form-group-inline {
      display: flex;
      align-items: center;
      gap: 15px;
      min-width: 200px;
      max-width: 100%; /* éviter débordement */
      flex-shrink: 1; /* permet de rétrécir si besoin */
      flex-wrap: nowrap;
    }
    .input-wrapper {
      position: relative;
      width: 200px;
    }
    .form-control {
      width: 100%;
      height: 38px;
      padding: 8px 30px 8px 12px;
      border: 1px solid #ccc;
      border-radius: 4px;
      box-sizing: border-box;
      font-size: 14px;
      line-height: 1.2;
    }
    .remove-icon {
      position: absolute;
      right: 8px;
      top: 50%;
      transform: translateY(-50%);
      cursor: pointer;
      color: #999;
      font-size: 14px;
      user-select: none;
    }
    .remove-icon:hover {
      color: #333;
    }
    .btn-inline {
      min-width: auto;
      width: auto;
      align-self: center;
    }
    .btn-plus {
      background-color: #f5f5f5; /* gris clair */
      border: 1px solid #ccc;
      color: black;
      font-weight: 700;  /* un peu plus bold */
      font-size: 28px;
      line-height: 1;
      cursor: pointer;
      padding: 0;
      width: 38px;
      height: 38px;
      display: flex;
      align-items: center;
      justify-content: center;
      user-select: none;
      border-radius: 4px;
      transition: background-color 0.2s ease;
    }
    .btn-plus:hover {
      background-color: #e0e0e0;
    }
    .select-dropdown {
      width: 180px;
      height: 38px;
      font-size: 14px;
      border-radius: 4px;
      border: 1px solid #ccc;
    }
    .btn-search {
      height: 38px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 12px;
      font-size: 18px;
      line-height: 1;
      border-radius: 4px;
    }
  `]
})
export class DynamicSearchFormComponent implements OnInit {
  @Input() searchFields: SearchField[] = [];
  @Input() onSubmitFn?: (formValue: any) => void;

  searchForm!: FormGroup;
  visibleFields: SearchField[] = [];
  hiddenFields: SearchField[] = [];
  showSelect = false;

  ngOnInit(): void {
    this.searchForm = new FormGroup({});
    this.visibleFields = this.searchFields.slice(0, 3);
    this.hiddenFields = this.searchFields.slice(3);

    this.searchFields.forEach(field => {
      this.searchForm.addControl(field.key, new FormControl(''));
    });
  }

  toggleSelect(): void {
    this.showSelect = !this.showSelect;
  }

  onAddField(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const key = select.value;
    if (!key) return;

    const index = this.hiddenFields.findIndex(f => f.key === key);
    if (index >= 0) {
      this.visibleFields.push(this.hiddenFields[index]);
      this.hiddenFields.splice(index, 1);
    }
    this.showSelect = false;
    select.value = '';
  }

  removeField(field: SearchField): void {
    this.searchForm.get(field.key)?.setValue('');
    this.hiddenFields.push(field);
    this.visibleFields = this.visibleFields.filter(f => f.key !== field.key);
  }

  onSearch(): void {
    if (this.onSubmitFn) {
      this.onSubmitFn(this.searchForm.value);
    }
  }
}
