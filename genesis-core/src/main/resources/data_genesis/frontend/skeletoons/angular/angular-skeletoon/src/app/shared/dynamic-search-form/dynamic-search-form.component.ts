import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

export interface SearchField {
  key: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'select';
  options?: { value: any; label: any }[];
}

@Component({
  selector: 'app-dynamic-search-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="filter-container">
      <form [formGroup]="searchForm" (ngSubmit)="onSearch()" class="search-form">

        <!-- Plus button et champs dynamiques -->
        <div *ngIf="hiddenFields.length > 0" class="form-group-inline btn-inline">
          <button type="button" class="btn-plus" (click)="toggleSelect()">＋</button>
          <select *ngIf="showSelect" (change)="onAddField($event)" class="form-control select-dropdown">
            <option value="">Select a field...</option>
            <option *ngFor="let field of hiddenFields" [value]="field.key">{{ field.label }}</option>
          </select>
        </div>

        <!-- Champs visibles -->
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

        <!-- Apply button -->
        <div class="form-group-inline btn-inline">
          <button type="button" class="btn-apply" (click)="searchForm.valid && onSearch()">Apply</button>
        </div>

        <!-- Rows selector -->
        <div class="rows-selector">
          <label for="rowsSelect">Showing:</label>
          <select [formControl]="rowsControl" id="rowsSelect" class="form-control">
            <option value="12">12</option>
            <option *ngFor="let r of rowsOptions" [value]="r">{{ r }}</option>
          </select>
        </div>

      </form>
    </div>
  `,
  styles: [`
    /* Conteneur principal */
    .filter-container {
      display: flex;
      width: 100%;
    }

    /* Formulaire */
    .search-form {
      display: flex;
      align-items: center;
      gap: 15px;
      flex-wrap: wrap;
      width: 100%;
    }

    /* Groupes de champs en ligne */
    .form-group-inline {
      display: flex;
      align-items: center;
      gap: 15px;
      min-width: 150px;
      flex-wrap: nowrap;
    }

    /* Wrapper des inputs pour positionner l'icône */
    .input-wrapper {
      position: relative;
      width: 150px;
    }

    /* Inputs et selects génériques */
    .form-control {
      width: 100%;
      height: 30px;
      padding: 5px 28px 5px 10px;
      border: none;
      border-radius: 4px;
      box-sizing: border-box;
      font-size: 14px;
      line-height: 1.2;
      box-shadow: 0px 1px 4px rgba(0,0,0,0.15);
    }

    /* Icône pour retirer un champ */
    .remove-icon {
      position: absolute;
      right: 10%;
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

    /* Boutons inline */
    .btn-inline {
      min-width: auto;
      width: auto;
      align-self: center;
    }

    /* Bouton "+" pour ajouter un champ */
    .btn-plus {
      background-color: white;
      border: none;
      color: black;
      font-weight: 700;
      font-size: 28px;
      cursor: pointer;
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      box-shadow: 0px 2px 6px rgba(0,0,0,0.15);
      transition: all 0.2s ease;
    }
    .btn-plus:hover {
      background-color: #f9f9f9;
      box-shadow: 0px 4px 8px rgba(0,0,0,0.2);
    }

    /* Dropdown pour les champs cachés */
    .select-dropdown {
      width: 150px;
      height: 30px;
      font-size: 14px;
      border-radius: 4px;
      border: none;
      box-shadow: 0px 1px 4px rgba(0,0,0,0.15);
    }

    /* Bouton Apply */
    .btn-apply {
      height: 30px;
      padding: 0 10px;
      font-size: 14px;
      line-height: 1.2;
      border-radius: 4px;
      border: none;
      background-color: #f0ededff;
      box-shadow: 0 1px 4px rgba(0,0,0,0.1);
      cursor: pointer;
      flex-shrink: 0;
    }
    .btn-apply:hover {
      background-color: #dededeff;
    }

    /* Rows selector */
    .rows-selector {
      display: flex;
      align-items: center;
      margin-left: auto;
      margin-right: 1%;
      gap: 5px;
      width: 10%;
      min-width: 50px;
    }
    .rows-selector select {
      width: 100%;
      height: 28px;
      font-size: 14px;
      border-radius: 4px;
      border: none;
      box-shadow: 0 1px 4px rgba(0,0,0,0.15);
      box-sizing: border-box;
      padding: 0 3px;
      text-align: center;
      text-align-last: center;
    }
  `]
})
export class DynamicSearchFormComponent implements OnInit {
  @Input() searchFields: SearchField[] = [];
  @Input() onRowsChange: (rows: number) => void = () => {};
  @Input() onSubmitFn?: (formValue: any) => void;

  searchForm!: FormGroup;
  visibleFields: SearchField[] = [];
  hiddenFields: SearchField[] = [];
  showSelect = false;

  rowsOptions = [3, 5, 10, 15, 20, 25, 50];
  rowsControl!: FormControl;

  ngOnInit(): void {
    // Valeur par défaut 12
    this.rowsControl = new FormControl(12);

    this.rowsControl.valueChanges.subscribe(val => {
      if (this.onRowsChange) this.onRowsChange(Number(val));
    });

    this.searchForm = new FormGroup({});
    this.visibleFields = this.searchFields.slice(0, 3);
    this.hiddenFields = this.searchFields.slice(3);

    this.searchFields.forEach(field => {
      this.searchForm.addControl(field.key, new FormControl(''));
    });
  }

  toggleSelect(): void { this.showSelect = !this.showSelect; }

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
    if (this.onSubmitFn) this.onSubmitFn(this.searchForm.value);
  }
}