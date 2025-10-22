import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MotherComponent } from '../mother-component/mother.component';

export interface SearchField {
  key: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'select' | 'datetime-local' | 'time';
  options?: { value: any; label: any }[];
}

@Component({
  selector: 'app-dynamic-search-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="filter-container">
      <form [formGroup]="searchForm" (ngSubmit)="onSearch()" class="search-form">
        <label class="label">{{this.content.search.simple_filter}}:</label>
        <div *ngIf="hiddenFields.length > 0" class="form-group-inline btn-inline">
          <button type="button" class="btn-plus" (click)="toggleSelect()">＋</button>
          <select *ngIf="showSelect" (change)="onAddField($event)" class="form-control select-dropdown">
            <option value="">{{this.content.search.selection}}...</option>
            <option *ngFor="let field of hiddenFields" [value]="field.key">{{ field.label }}</option>
          </select>
        </div>

        <div *ngFor="let field of visibleFields" class="form-group-inline">
          <div class="input-wrapper">
          <ng-container [ngSwitch]="field.type">
            <input *ngSwitchCase="'text'" [formControlName]="field.key" type="text" class="form-control" [placeholder]="field.label">
            <input *ngSwitchCase="'number'" [formControlName]="field.key" type="number" class="form-control" [placeholder]="field.label">
            <input *ngSwitchCase="'date'" [formControlName]="field.key" type="date" class="form-control" [placeholder]="field.label">
            <input *ngSwitchCase="'time'" [formControlName]="field.key" type="time" class="form-control" [placeholder]="field.label">
            <input *ngSwitchCase="'datetime-local'" [formControlName]="field.key" type="datetime-local" class="form-control" [placeholder]="field.label">

            <ng-container *ngSwitchCase="'select'">
              <select *ngIf="field.options && field.options.length <= 8"
                      [formControlName]="field.key"
                      class="form-control">
                <option value="">{{ field.label }}</option>
                <option *ngFor="let option of field.options" [value]="option.value">{{ option.label }}</option>
              </select>
              <ng-container *ngIf="field.options && field.options.length > 8">
                <input type="text"
                      class="form-control"
                      [formControlName]="field.key"
                      [attr.list]="'list-' + field.key"
                      [placeholder]="field.label"
                />
                <datalist [id]="'list-' + field.key">
                  <option *ngFor="let option of field.options" [value]="option.label"></option>
                </datalist>
              </ng-container>
            </ng-container>
            </ng-container>
            <span class="remove-icon" (click)="removeField(field)">✖</span>
          </div>
        </div>

        <div class="form-group-inline btn-inline">
          <button type="button" class="btn-apply" (click)="searchForm.valid && onSearch()">{{this.content.button.applySearch}}</button>
        </div>

        <div class="rows-selector">
          <label for="rowsInput">{{this.content.pagination.sizeLabel}}</label>
          <input
            type="number"
            id="rowsInput"
            class="form-control"
            [formControl]="rowsControl"
            min="1"
          />
        </div>

      </form>
    </div>
  `,
  styles: [`
    .filter-container {
      display: flex;
      width: 100%;
    }
    .search-form {
      display: flex;
      align-items: center;
      gap: 15px;
      flex-wrap: wrap;
      width: 100%;
    }
    .form-group-inline {
      display: flex;
      align-items: center;
      gap: 15px;
      min-width: 150px;
      flex-wrap: nowrap;
    }
    .input-wrapper {
      position: relative;
      width: 150px;
    }
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
    .btn-inline {
      min-width: auto;
      width: auto;
      align-self: center;
    }
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
    .select-dropdown {
      width: 150px;
      height: 30px;
      font-size: 14px;
      border-radius: 4px;
      border: none;
      box-shadow: 0px 1px 4px rgba(0,0,0,0.15);
    }
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
    .rows-selector {
      display: flex;
      align-items: center;
      margin-left: auto;
      margin-right: 1%;
      gap: 5px;
      width: 10%;
      min-width: 50px;
    }
    .rows-selector input {
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
    .label
    {
      color: #555;
      font-size: 16px;
    }
  `]
})
export class DynamicSearchFormComponent extends MotherComponent implements OnInit {
  @Input() searchFields: SearchField[] = [];
  @Input() onRowsChange: (rows: number) => void = () => {};
  @Input() onSubmitFn?: (formValue: any) => void;

  searchForm!: FormGroup;
  visibleFields: SearchField[] = [];
  hiddenFields: SearchField[] = [];
  showSelect = false;

  rowsOptions = [3, 5, 10, 15, 20, 25, 50];
  rowsControl!: FormControl;

  mapLabelToValue(field: SearchField) {
    const label = this.searchForm.get(field.key)?.value;
    const option = field.options?.find(opt => opt.label === label);

    if (option) {
      this.searchForm.get(field.key)?.setValue(option.value, { emitEvent: false });
    }
  }
  override ngOnInit(): void {
    super.ngOnInit()
    this.rowsControl = new FormControl(12);

    this.rowsControl.valueChanges.subscribe(val => {
      console.log(val);
      var number=val!=null?val:"12";
      if (this.onRowsChange) this.onRowsChange(Number(number));
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
    const payload = { ...this.searchForm.value };

    this.searchFields.forEach(field => {
      if (field.type === 'select' && field.options && field.options.length > 8) {
        const label = payload[field.key]; // <-- on lit directement dans le payload
        const option = field.options.find(o => o.label === label);

        if (option) {
          payload[field.key] = option.value; // on remplace par l'ID
        } else {
          payload[field.key] = null; // pas de correspondance
        }
      }
    });
    console.log(payload)
    if (this.onSubmitFn) this.onSubmitFn(payload);
  }
}
