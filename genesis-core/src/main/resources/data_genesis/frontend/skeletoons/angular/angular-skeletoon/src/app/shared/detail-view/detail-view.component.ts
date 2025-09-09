import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
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
  @Input() LinkContext:string="";

  data: Record<string, any> = {};

  showConfirmation = false;

  constructor(private router: Router) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['object'] && this.object) {
      this.data = objectToRecord(this.object);
    }
  }

  get entries() {
    return Object.entries(this.data);
  }

  onDeleteClick() {
    if (this.id && this.deletfn) {
      this.showConfirmation = true;
    } else {
      console.warn('Delete function or object ID missing');
    }
  }

  onUpdateClick() {
    if (this.id ) {
      this.router.navigate(['/'+this.LinkContext+"/modify", this.id]);
    }
  }

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
