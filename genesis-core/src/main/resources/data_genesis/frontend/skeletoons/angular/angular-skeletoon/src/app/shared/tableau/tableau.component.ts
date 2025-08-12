import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmationBoxComponent } from '../confirmation-box.component/confirmation-box.component'; // chemin à ajuster

@Component({
  selector: 'app-tableau',
  standalone: true,
  imports: [CommonModule, ConfirmationBoxComponent],
  template: `
    <table class="styled-table">
      <thead>
        <tr>
          <th *ngFor="let col of colonnes">{{ col }}</th>
          <th *ngIf="!isView">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let ligne of donnees">
          <td *ngFor="let valeur of ligne">{{ valeur }}</td>
          <td class="actions" *ngIf="!isView">
            <button (click)="viewFn?.(ligne)" title="View" aria-label="View">
              <i class="bi bi-file-text"></i>
            </button>
            <button (click)="editFn?.(ligne)" title="Edit" aria-label="Edit">
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
    /* styles du tableau identiques à ta version */
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
      background-color: #e6f0ff;
    }
    .styled-table th,
    .styled-table td {
      padding: 0.42rem 1rem;
      text-align: left;
      border-bottom: 1px solid #e2e8f0;
    }
    .styled-table th {
      font-weight: 600;
      color: #334155;
    }
    .styled-table tbody tr:hover {
      background-color: #f9fafb;
    }
    .actions {
      display: flex;
      gap: 0.5rem;
    }
    .actions button {
      background-color: #f1f5f9;
      border: none;
      border-radius: 0.375rem;
      padding: 0.375rem;
      cursor: pointer;
      transition: background-color 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      color: #333;
    }
    .actions button:hover {
      background-color: #e2e8f0;
      color: #000;
    }
  `]
})
export class TableauComponent {
  @Input() colonnes: string[] = [];
  @Input() donnees: any[][] = [];
  @Input() viewFn?: (ligne: any[]) => void;
  @Input() editFn?: (ligne: any[]) => void;
  @Input() deleteFn?: (ligne: any[]) => void;
  @Input() isView: boolean = false;

  showConfirmation = false;
  selectedItem: any[] | null = null;
  selectedItemName = '';

  openConfirmation(ligne: any[]) {
    this.selectedItem = ligne;
    this.selectedItemName = ligne[1] || 'Item';
    this.showConfirmation = true;
  }

  confirmDelete = (item: any) => {
    if (this.deleteFn) {
      this.deleteFn(item);
    }
    this.showConfirmation = false;
  };

  cancelDelete = () => {
    this.showConfirmation = false;
  };
}
