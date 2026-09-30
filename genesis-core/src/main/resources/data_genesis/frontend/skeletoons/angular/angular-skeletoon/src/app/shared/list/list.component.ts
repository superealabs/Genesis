import { Component, HostListener, Input,OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ConfirmationBoxComponent } from '../confirmation-box.component/confirmation-box.component';
import { Language,LanguageService } from '../services/language/language.service';
import {buildFileSource, downloadFile, getFileSize, getGeneratedFileName, isImageContent} from '../file-utils';

export interface Column {
  type: string;
  label: string;
  fieldName: string;
}

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule, ConfirmationBoxComponent],
  template: `
    <table class="styled-table">
      <thead>
        <tr>
          <th *ngFor="let col of columns; let i = index" (click)="setActiveColumn(i)">
            {{ col.label }}
            <span *ngIf="activeColumn === i">
              <i class="bi" [ngClass]="sortAsc ? 'bi-caret-down-fill' : 'bi-caret-up-fill'"></i>
            </span>
          </th>
          <th *ngIf="!isView">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let ligne of datas">
          <td *ngFor="let col of columns; let i = index">
            <ng-container *ngIf="isFileColumn(col.type); else normalCell">
              <div *ngIf="ligne[i] && isImageContent(ligne[i]); else nonImageFile" class="file-cell">
                <img
                  [src]="buildFileSource(ligne[i])"
                  [alt]="getGeneratedFileName(ligne[i])"
                  class="file-preview"
                  role="button"
                  tabindex="0"
                  (click)="openPreview(ligne[i], getGeneratedFileName(ligne[i]))"
                  (keydown.enter)="openPreview(ligne[i], getGeneratedFileName(ligne[i]))"
                />
                <span class="file-size">{{ getFileSize(ligne[i]) }}</span>
                <button type="button" class="file-download" (click)="downloadAsset(ligne[i])" [attr.aria-label]="'Télécharger ' + getGeneratedFileName(ligne[i])" title="Télécharger l'image">
                  <i class="bi bi-download"></i> {{ downloadedFile === getGeneratedFileName(ligne[i]) ? 'Téléchargement…' : 'Télécharger' }}
                </button>
              </div>
              <ng-template #nonImageFile>
                <div *ngIf="ligne[i]; else noFile" class="file-cell">
                  <span>{{ getGeneratedFileName(ligne[i]) }}</span>
                  <span class="file-size">{{ getFileSize(ligne[i]) }}</span>
                <button *ngIf="ligne[i]" type="button" (click)="downloadAsset(ligne[i])" [attr.aria-label]="'Télécharger ' + getGeneratedFileName(ligne[i])" title="Télécharger">
                  <i class="bi bi-download"></i> {{ downloadedFile === getGeneratedFileName(ligne[i]) ? 'Téléchargement…' : '' }}
                </button>
                </div>
                <ng-template #noFile><span>Aucun fichier</span></ng-template>
              </ng-template>
            </ng-container>
            <ng-template #normalCell>
              {{ langService.formatValue(ligne[i]) }}
            </ng-template>
          </td>
          <td class="actions" *ngIf="!isView">
            <button (click)="redirect(routeToDetail, ligne[getIdIndex()])" title="View" aria-label="View">
                <i class="bi bi-file-text"></i>
            </button>
            <button (click)="redirect(routeToModify, ligne[getIdIndex()])" title="Edit" aria-label="Edit">
                <i class="bi bi-pencil"></i>
            </button>
            <button (click)="openConfirmation(ligne)" title="Delete" aria-label="Delete">
              <i class="bi bi-trash"></i>
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div *ngIf="previewSource" class="image-preview-overlay" role="dialog" aria-modal="true" [attr.aria-label]="previewAlt" (click)="closePreview()">
      <button type="button" class="preview-close" aria-label="Fermer la prévisualisation" (click)="closePreview()">×</button>
      <img [src]="previewSource" [alt]="previewAlt" (click)="$event.stopPropagation()" />
    </div>

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
    .file-preview {
      width: 80px;
      height: 60px;
      object-fit: cover;
      border-radius: 4px;
      display: block;
      cursor: zoom-in;
    }
    .file-cell { display: flex; align-items: center; gap: .5rem; flex-wrap: wrap; }
    .file-size { color: #64748b; font-size: .75rem; }
    .file-download { cursor: pointer; }
    .image-preview-overlay { position: fixed; inset: 0; z-index: 1000; display: grid; place-items: center; padding: 1rem; background: rgba(0,0,0,.82); }
    .image-preview-overlay img { max-width: 90vw; max-height: 90vh; object-fit: contain; border-radius: 8px; }
    .preview-close { position: absolute; top: 1rem; right: 1rem; color: white; background: transparent; border: 0; font-size: 2rem; cursor: pointer; }
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
export class ListComponent implements OnInit{
  readonly buildFileSource = buildFileSource;
  readonly getGeneratedFileName = getGeneratedFileName;
  readonly getFileSize = getFileSize;
  readonly isImageContent = isImageContent;
  previewSource: string | null = null;
  previewAlt = '';
  downloadedFile: string | null = null;
  private downloadFeedbackTimeout?: ReturnType<typeof setTimeout>;
  @Input() columns: Column[] = [];
  @Input() datas: any[] = [];
  @Input() routeToDetail: string = 'entity';
  @Input() routeToModify: string = 'entity';
  @Input() editFn?: (ligne: any) => void;
  @Input() deleteFn?: (ligne: any) => void;
  @Input() isView: boolean = false;
  @Input() sortFn?: (colIndex: number, asc: boolean) => void;
  @Input() language: Language = this.langService.currentLanguage;

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

  constructor(private router: Router,public langService: LanguageService) {}

  isFileColumn(type: string): boolean {
    return ['uint8array', 'bytea', 'blob', 'varbinary', 'byte[]', 'bytearray', 'file'].includes(type.replace(/\s/g, '').toLowerCase());
  }

  openPreview(content: unknown, alt: string): void { this.previewSource = buildFileSource(content); this.previewAlt = alt; }
  closePreview(): void { this.previewSource = null; }
  downloadAsset(content: unknown): void {
    const fileName = getGeneratedFileName(content);
    downloadFile(content, fileName);
    this.downloadedFile = fileName;
    if (this.downloadFeedbackTimeout) clearTimeout(this.downloadFeedbackTimeout);
    this.downloadFeedbackTimeout = setTimeout(() => this.downloadedFile = null, 1600);
  }
  @HostListener('document:keydown.escape') onEscape(): void { this.closePreview(); }

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
    this.selectedItemName = ligne[this.columns[1]?.fieldName] || 'Item';
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

  getIdIndex(): number {
      const index = this.columns.findIndex(col => col.fieldName.toLowerCase() === 'id');
      return index !== -1 ? index : 0; // Fallback sur la première colonne si 'id' non trouvé
  }

}
