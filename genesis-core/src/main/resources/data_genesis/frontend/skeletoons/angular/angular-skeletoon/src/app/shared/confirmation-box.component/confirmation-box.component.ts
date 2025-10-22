import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MotherComponent } from '../mother-component/mother.component';

@Component({
  selector: 'app-confirmation-box',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-box.component.html',
  styleUrls: ['./confirmation-box.component.css']
})
export class ConfirmationBoxComponent extends MotherComponent implements OnInit{
  @Input() message: string = 'Are you sure you want to delete this item?';
  @Input() value: any;
  @Input() onConfirm!: (value: any) => void;
  @Input() onCancel!: () => void;

  confirm() {
    if (this.onConfirm) {
      this.onConfirm(this.value);
    }
  }

  cancel() {
    if (this.onCancel) {
      this.onCancel();
    }
  }
}
