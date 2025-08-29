import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { objectToRecord } from '../../../utilities/utilities';
import { ConfirmationBoxComponent } from '../confirmation-box.component/confirmation-box.component';

@Component({
  selector: 'app-detail-view',
  standalone: true,
  imports: [CommonModule, ConfirmationBoxComponent],
  templateUrl: './detail-view.component.html',
  styleUrls: ['./detail-view.component.css']
})
export class DetailViewComponent implements OnChanges {
  @Input() object: any = {};
  @Input() id?: string = "";
  @Input() deletfn?: (id: string) => void;

  data: Record<string, any> = {};

  // Gestion confirmation
  showConfirmation = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['object'] && this.object) {
      this.data = objectToRecord(this.object);
    }
  }

  get entries() {
    return Object.entries(this.data);
  }

  // Ouvre la confirmation avant suppression
  onDeleteClick() {
    if (this.id && this.deletfn) {
      this.showConfirmation = true;
    } else {
      console.warn('Delete function or object ID missing');
    }
  }

  // Confirmer la suppression
  confirmDelete = () => {
    if (this.id && this.deletfn) {
      this.deletfn(this.id);
    }
    this.showConfirmation = false;
  };

  // Annuler la suppression
  cancelDelete = () => {
    this.showConfirmation = false;
  };
}
