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
  @Input() form!: FormGroup;
  @Input() isLoading: boolean = false;
  @Input() initialData: any = {};
  @Input() validationErrors: any={};

  submit(): void {
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.submitFn) return;
    const payload = { ...this.form.value };
    this.fields.forEach(field => {
      if (field.type === 'select' && field.options && field.options.length > 8 && Object.keys(this.initialData).length === 0) {
        const label = payload[field.name];
        const option = field.options.find(o => o.label === label);
        if (option) {
          payload[field.name] = option.value;
        } else {
          payload[field.name] = null;
        }
      }
    });
    this.submitFn(payload);
  }

}
