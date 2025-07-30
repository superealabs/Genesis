import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tableau',
  standalone: true,
  imports: [CommonModule],
  template: `
    <table class="styled-table">
      <thead>
        <tr>
          <th *ngFor="let col of colonnes">{{ col }}</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let ligne of donnees">
          <td *ngFor="let valeur of ligne">{{ valeur }}</td>
          <td class="actions">
            <button (click)="viewFn?.(ligne)" title="View" aria-label="View">
              <i class="bi bi-file-text"></i>
            </button>
            <button (click)="editFn?.(ligne)" title="Edit" aria-label="Edit">
              <i class="bi bi-pencil"></i>
            </button>
            <button (click)="deleteFn?.(ligne)" title="Delete" aria-label="Delete">
              <i class="bi bi-trash"></i>
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [`
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
      background-color: #e6f0ff; /* bleu pastel très clair */
    }

    .styled-table th,
    .styled-table td {
      padding: 0.42rem 1rem; /* ~6.7px vertical, 16px horizontal */
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
      gap: 0.5rem; /* 8px */
    }

    .actions button {
      background-color: #f1f5f9;
      border: none;
      border-radius: 0.375rem; /* 6px */
      padding: 0.375rem; /* 6px */
      cursor: pointer;
      transition: background-color 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem; /* taille des icônes */
      color: #333;
    }

    .actions button:hover {
      background-color: #e2e8f0;
      color: #000;
    }

    .actions i {
      pointer-events: none; /* évite que clic passe à l’icône */
    }
  `]
})
export class TableauComponent {
  @Input() colonnes: string[] = [];
  @Input() donnees: any[][] = [];

  @Input() viewFn?: (ligne: any[]) => void;
  @Input() editFn?: (ligne: any[]) => void;
  @Input() deleteFn?: (ligne: any[]) => void;
}
