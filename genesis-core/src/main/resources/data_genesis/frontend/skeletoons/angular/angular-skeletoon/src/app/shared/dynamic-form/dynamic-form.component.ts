import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup } from '@angular/forms';
import { DynamicFieldComponent, FieldConfig } from '../dynamic-field/dynamic-field.component';
import { LinkButtonComponent } from '../link-button/link-button.component';
import { LoadingIconComponent } from '../loading-icon/loading-icon.component';

@Component({
  selector: 'app-dynamic-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DynamicFieldComponent, LinkButtonComponent, LoadingIconComponent],
  templateUrl: './dynamic-form.component.html',
  styleUrls: ['./dynamic-form.component.css']
})
export class DynamicFormComponent {

  @Input() fields: FieldConfig[] = [];
  @Input() submitFn!: (formValue: any) => void;
  @Input() form!: FormGroup; // le FormGroup est fourni depuis le parent
  @Input() isLoading: boolean = false;

  submit(): void {
    if (this.form.valid && this.submitFn) {
      this.submitFn(this.form.value);
    } else {
      this.form.markAllAsTouched();
    }
  }
}
