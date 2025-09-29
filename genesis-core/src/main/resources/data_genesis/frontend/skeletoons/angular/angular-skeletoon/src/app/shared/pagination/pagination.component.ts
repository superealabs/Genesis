import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MotherComponent } from '../mother-component/mother.component';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <nav *ngIf="totalPages > 1" class="pagination-container">
      <button
        class="arrow"
        (click)="changePage(currentPage - 1)"
        [disabled]="currentPage === 1"
        aria-label="Page précédente"
      >&lt;</button>

      <button *ngIf="canShiftLeft()" (click)="shiftPageWindowLeft()" title="Pages précédentes">...</button>

      <button
        *ngFor="let page of displayedPages"
        (click)="changePage(page)"
        [class.active]="page === currentPage">
        {{ page }}
      </button>

      <button *ngIf="canShiftRight()" (click)="shiftPageWindowRight()" title="Pages suivantes">...</button>

      <button
        class="arrow"
        (click)="changePage(currentPage + 1)"
        [disabled]="currentPage === totalPages"
        aria-label="Page suivante"
      >&gt;</button>

      <!-- Input "Aller à la page" à droite -->
      <div class="goto-container">
        <label for="gotoPage">{{this.content.pagination.goToLabel}} :</label>
        <input
          id="gotoPage"
          type="number"
          [min]="1"
          [max]="totalPages"
          [(ngModel)]="currentPage"
          (keydown.enter)="changePage(currentPage)"
        />
        <button (click)="changePage(currentPage)">{{this.content.button.go}}</button>
      </div>
    </nav>
  `,
  styles: [`
    .pagination-container {
      display: flex;
      justify-content: flex-start;
      padding: 0.5rem 0;
      margin-top: 1%;
      font-family: Arial, sans-serif;
      align-items: center;
    }

    .pagination-container button {
      padding: 0.15rem 0.6rem;
      font-size: 0.85rem;
      height: 28px;
      border: 1px solid #d1d5db;
      background: white;
      cursor: pointer;
      border-radius: 6px;
      color: #374151;
      min-width: 48px;
      transition: background-color 0.15s, border-color 0.15s, color 0.15s;
      line-height: 1.2;
      text-align: center;
      user-select: none;
      margin-right: 18px;
    }

    .pagination-container button:last-child {
      margin-right: 0;
    }

    .pagination-container button.arrow {
      background-color: #e0e0e0;
      color: #374151;
      border-color: transparent;
      min-width: 36px;
    }

    .pagination-container button.arrow:hover:not(:disabled) {
      background-color: #d0d0d0;
      border-color: #a8a8a8;
    }

    button.active {
      background-color: #f0f0f0;
      color: #111;
      border-color: #ccc;
      font-weight: 600;
    }

    button:hover:not(:disabled):not(.active):not(.arrow) {
      background-color: #f9f9f9;
      border-color: #bbb;
      color: #222;
    }

    button:disabled {
      cursor: not-allowed;
      opacity: 0.4;
      background-color: white;
      border-color: #d1d5db;
      color: #9ca3af;
    }

    .goto-container {
      display: flex;
      align-items: center;
      margin-left: auto;
    }

    .goto-container label {
      margin-right: 0.5rem;
      font-size: 1.0rem;
      color: #374151;
    }

    .goto-container input {
      width: 60px;
      padding: 0.15rem 0.5rem;
      font-size: 0.85rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      margin-right: 0.5rem;
      text-align: center;
      height: 22px;
      width:30px;
      line-height: 1.2;
    }

    .goto-container button {
      padding: 0.15rem 0.6rem;
      font-size: 0.85rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      background:  #cfcfcfff;
      cursor: pointer;
      transition: background-color 0.15s, border-color 0.15s, color 0.15s;
    }

    .goto-container button:hover {
      background-color: #f9f9f9;
    }
  `]
})
export class PaginationComponent extends MotherComponent implements OnInit{
  @Input() totalItems: number = 0;
  @Input() itemsPerPage: number = 12;
  @Input() currentPage: number = 1;
  @Input() onPageRangeChange!: (page:number) => void;

  maxVisiblePages: number = 4;
  private pageWindowStartIndex: number = 0;

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.itemsPerPage);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  get displayedPages(): number[] {
    return this.pages.slice(this.pageWindowStartIndex, this.pageWindowStartIndex + this.maxVisiblePages);
  }

  canShiftRight(): boolean {
    return this.pageWindowStartIndex + this.maxVisiblePages < this.totalPages;
  }

  canShiftLeft(): boolean {
    return this.pageWindowStartIndex > 0;
  }

  shiftPageWindowRight(): void {
    if (this.canShiftRight()) {
      this.pageWindowStartIndex += this.maxVisiblePages;
      const windowPages = this.displayedPages;
      if (!windowPages.includes(this.currentPage)) {
        this.changePage(windowPages[0]);
      }
    }
  }

  shiftPageWindowLeft(): void {
    if (this.canShiftLeft()) {
      this.pageWindowStartIndex -= this.maxVisiblePages;
      if (this.pageWindowStartIndex < 0) this.pageWindowStartIndex = 0;
      const windowPages = this.displayedPages;
      if (!windowPages.includes(this.currentPage)) {
        this.changePage(windowPages[windowPages.length - 1]);
      }
    }
  }

  changePage(page: number): void {
    if (page < 1) page = 1;
    if (page > this.totalPages) page = this.totalPages;

    this.currentPage = page;

    if (this.onPageRangeChange) {
      this.onPageRangeChange(page);
    }
  }
}
