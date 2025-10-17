import { Component, Input,OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ConfirmationBoxComponent } from '../confirmation-box.component/confirmation-box.component';
import { Language,LanguageService } from '../services/language/language.service';


export interface Colonne {
  type: 'string' | 'number' | 'Date';
  label: string;
  fieldName: string;
}

@Component({
  selector: 'app-tableau',
  standalone: true,
  imports: [CommonModule, ConfirmationBoxComponent],
  template: `
    <table class="styled-table">
      <thead>
        <tr>
          <th *ngFor="let col of colonnes; let i = index" (click)="setActiveColumn(i)">
            {{ col.label }}
            <span *ngIf="activeColumn === i">
              <i class="bi" [ngClass]="sortAsc ? 'bi-caret-down-fill' : 'bi-caret-up-fill'"></i>
            </span>
          </th>
          <th *ngIf="!isView">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let ligne of donnees">
          <td *ngFor="let col of colonnes; let i = index">
            {{ formatValue(ligne[i], col.type) }}
          </td>
          <td class="actions" *ngIf="!isView">
            <button (click)="redirect(routeToDetail, ligne[0])" title="View" aria-label="View">
              <i class="bi bi-file-text"></i>
            </button>
            <button (click)="redirect(routeToModify, ligne[0])" title="Edit" aria-label="Edit">
              <i class="bi bi-pencil"></i>
            </button>
            <button (click)="openConfirmation(ligne)" title="Delete" aria-label="Delete">
              <i class="bi bi-trash"></i>
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <app-confirmation-box
      *ngIf="showConfirmation"
      [message]="'Are you sure you want to delete <b>' + selectedItemName + '</b>?'"
      [value]="selectedItem"
      [onConfirm]="confirmDelete"
      [onCancel]="cancelDelete">
    </app-confirmation-box>
  `,
  styles: [`
    @import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css');
    .styled-table {
      width: 100%;
      border-collapse: collapse;
      font-family: 'Segoe UI', sans-serif;
      font-size: 0.875rem;
      color: #333;
      background-color: #fff;
      border-radius: 0.75rem;
      overflow: hidden;
      box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.05);
    }
    .styled-table thead {
      background-color: var(--bg-hover);
      cursor: pointer;
    }
    .styled-table th,
    .styled-table td {
      padding: 0.378rem 0.9rem;
      text-align: left;
      border-bottom: 1px solid #e2e8f0;
    }
    .styled-table th {
      font-weight: 600;
      color: #334155;
      user-select: none;
    }
    .styled-table tbody tr:hover {
      background-color: #f9fafb;
    }
    .actions {
      display: flex;
      gap: 0;
      transform: scale(1);
    }
    .actions button {
      background-color: #f1f5f9;
      border: none;
      border-radius: 0;
      padding: 0.25rem;
      font-size: 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #333;
    }
    .actions button:first-child {
      border-top-left-radius: 0.75rem;
      border-bottom-left-radius: 0.75rem;
    }
    .actions button:last-child {
      border-top-right-radius: 0.75rem;
      border-bottom-right-radius: 0.75rem;
    }
    .actions button:hover {
      background-color: #e2e8f0;
      color: #000;
    }
  `]
})
export class TableauComponent implements OnInit{
  @Input() colonnes: Colonne[] = [];
  @Input() donnees: any[] = [];
  @Input() routeToDetail: string = 'entity';
  @Input() routeToModify: string = 'entity';
  @Input() editFn?: (ligne: any) => void;
  @Input() deleteFn?: (ligne: any) => void;
  @Input() isView: boolean = false;
  @Input() sortFn?: (colIndex: number, asc: boolean) => void;
  @Input() language: Language = Language.EN;

  activeColumn: number = 0;
  sortAsc: boolean = true;

  showConfirmation = false;
  selectedItem: any | null = null;
  selectedItemName = '';

  ngOnInit() {
    this.langService.language$.subscribe(lang => {
      this.language= lang;
    });
  }

  constructor(private router: Router,private langService: LanguageService) {}

  formatNumber(value: number): string {
    if (value == null) return '';
    return new Intl.NumberFormat(this.language).format(value);
  }

  formatDate(value: Date | string | number): string {
    if (!value) return '';
    const date = new Date(value);
    const hasTime = date.getHours() !== 0 || date.getMinutes() !== 0 || date.getSeconds() !== 0;
    if (hasTime) {
      return new Intl.DateTimeFormat(this.language, {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).format(date);
    } else {
      return new Intl.DateTimeFormat(this.language, {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }).format(date);
    }
  }


  formatDateTime(value: Date | string | number): string {
    if (!value) return '';
    const date = new Date(value);
    return new Intl.DateTimeFormat(this.language, {
      year:'numeric', month:'2-digit', day:'2-digit',
      hour:'2-digit', minute:'2-digit', second:'2-digit'
    }).format(date);
  }

  formatTime(value: Date | string | number): string {
    if (!value) return '';
    const date = new Date(value);
    return new Intl.DateTimeFormat(this.language, { hour:'2-digit', minute:'2-digit', second:'2-digit' }).format(date);
  }

  formatValue(value: any, type: 'string' | 'number' | 'Date'): string {
    if (value == null) return '';
    switch(type) {
      case 'number': return this.formatNumber(value);
      case 'Date': return this.formatDate(value);
      case 'string': return value;
        console.log(value)
      default: return value;
    }
  }

  setActiveColumn(index: number) {
    if (this.activeColumn === index) {
      this.sortAsc = !this.sortAsc;
    } else {
      this.activeColumn = index;
      this.sortAsc = true;
    }
    if (this.sortFn) {
      this.sortFn(this.activeColumn, this.sortAsc);
    }
  }

  openConfirmation(ligne: any) {
    this.selectedItem = ligne;
    this.selectedItemName = ligne[this.colonnes[1]?.fieldName] || 'Item';
    this.showConfirmation = true;
  }

  confirmDelete = (item: any) => {
    if (this.deleteFn) this.deleteFn(item);
    this.showConfirmation = false;
  };

  cancelDelete = () => {
    this.showConfirmation = false;
  };

  redirect(route: string, id: any) {
    this.router.navigate([route, id]);
  }
  
}
