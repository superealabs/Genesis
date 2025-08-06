import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LinkButtonComponent } from '../link-button/link-button.component';

export interface SearchField {
  key: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'select';
  options?: { value: string; label: string }[];
}

@Component({
  selector: 'app-dynamic-search-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LinkButtonComponent],
  template: `
    <form [formGroup]="searchForm" (ngSubmit)="onSearch()" class="search-form">
      <div *ngFor="let field of searchFields" class="form-group-inline">
        <ng-container [ngSwitch]="field.type">
          <input
            *ngSwitchCase="'text'"
            [formControlName]="field.key"
            type="text"
            class="form-control"
            [placeholder]="field.label"
          >
          <input
            *ngSwitchCase="'number'"
            [formControlName]="field.key"
            type="number"
            class="form-control"
            [placeholder]="field.label"
          >
          <input
            *ngSwitchCase="'date'"
            [formControlName]="field.key"
            type="date"
            class="form-control"
            [placeholder]="field.label"
          >
          <select
            *ngSwitchCase="'select'"
            [formControlName]="field.key"
            class="form-control"
          >
            <option value="">{{ field.label }}</option>
            <option *ngFor="let option of field.options" [value]="option.value">{{ option.label }}</option>
          </select>
        </ng-container>
      </div>

      <!-- ✅ Submit button sans redirection -->
      <app-link-button
        [isButton]="true"
        [buttonLabel]="'Rechercher'"
        type="search"
        (onClick)="searchForm.valid && onSearch()"
      ></app-link-button>
    </form>
  `,
  styles: [`
    .search-form {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }

    .form-group-inline {
      width: 180px;
      min-width: 180px;
    }

    .form-control {
      width: 100%;
      padding: 8px 12px;
      border: 1px solid #ccc;
      border-radius: 4px;
      box-sizing: border-box;
      color: #333;
    }

    .form-control::placeholder {
      color: #999;
      transition: opacity 0.3s ease;
    }

    .form-control:not(:placeholder-shown)::placeholder {
      opacity: 0;
    }

    select.form-control {
      color: #666;
    }

    .btn-submit {
      cursor: pointer;
      user-select: none;
      display: inline-flex;
      align-items: center;
      padding: 10px 15px;
      font-size: 14px;
      background-color: #007bff;
      color: white;
      border-radius: 4px;
      text-decoration: none;
      gap: 5px;
      border: none;
      flex-shrink: 0;
    }

    .btn-submit:hover {
      background-color: #0056b3;
    }
  `]
})
export class DynamicSearchFormComponent implements OnInit {
  @Input() searchFields: SearchField[] = [];
  @Input() onSubmitFn?: (formValue: any) => void;

  searchForm!: FormGroup;

  ngOnInit(): void {
    this.searchForm = new FormGroup({});
    this.searchFields.forEach(field => {
      this.searchForm.addControl(field.key, new FormControl(''));
    });
  }

  onSearch(): void {
    if (this.onSubmitFn) {
      this.onSubmitFn(this.searchForm.value);
    }
  }
}
