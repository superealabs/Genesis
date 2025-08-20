import { Component, Input, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, AbstractControl, ValidatorFn, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; // pour *ngIf et *ngFor

export interface FieldConfig {
  type: 'text' | 'number' | 'date' | 'select' | 'textarea' | 'file';
  name: string;
  label: string;
  options?: { value: any; label: string }[];
  constraints?: {
    required?: boolean;
    notBlank?: boolean;
    minlength?: number;
    maxlength?: number;
    min?: number;
    max?: number;
    past?: boolean;
    pastOrPresent?: boolean;
    future?: boolean;
    futureOrPresent?: boolean;
    pattern?: string;
  };
}

@Component({
  selector: 'app-dynamic-field',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dynamic-field.component.html',
  styleUrls: ['./dynamic-field.component.css']
})
export class DynamicFieldComponent implements OnInit {
  @Input() field!: FieldConfig;
  @Input() form!: FormGroup;

  ngOnInit(): void {
    const validators: ValidatorFn[] = [];

    const c = this.field.constraints || {};

    if (c.required) validators.push(Validators.required);
    if (c.notBlank) validators.push(this.notBlankValidator);
    if (c.minlength) validators.push(Validators.minLength(c.minlength));
    if (c.maxlength) validators.push(Validators.maxLength(c.maxlength));
    if (c.min !== undefined) validators.push(Validators.min(c.min));
    if (c.max !== undefined) validators.push(Validators.max(c.max));
    if (c.pattern) validators.push(Validators.pattern(c.pattern));

    if (c.past) validators.push(this.pastValidator);
    if (c.pastOrPresent) validators.push(this.pastOrPresentValidator);
    if (c.future) validators.push(this.futureValidator);
    if (c.futureOrPresent) validators.push(this.futureOrPresentValidator);

    console.log(c);

    this.form.addControl(this.field.name, new FormControl('', validators));
  }

  getControl(): FormControl {
  return this.form.get(this.field.name) as FormControl;
}

  // === Validators personnalisés ===
  notBlankValidator(control: AbstractControl) {
    if (control.value != null && typeof control.value === 'string' && control.value.trim().length === 0) {
      return { notBlank: true };
    }
    return null;
  }

  pastValidator(control: AbstractControl) {
    if (!control.value) return null;
    return new Date(control.value) < new Date() ? null : { past: true };
  }

  pastOrPresentValidator(control: AbstractControl) {
    if (!control.value) return null;
    return new Date(control.value) <= new Date() ? null : { pastOrPresent: true };
  }

  futureValidator(control: AbstractControl) {
    if (!control.value) return null;
    return new Date(control.value) > new Date() ? null : { future: true };
  }

  futureOrPresentValidator(control: AbstractControl) {
    if (!control.value) return null;
    return new Date(control.value) >= new Date() ? null : { futureOrPresent: true };
  }

  // Gestion upload fichier
  onFileChange(event: any) {
    const file = event.target.files[0];
    this.form.patchValue({ [this.field.name]: file });
    this.form.get(this.field.name)?.updateValueAndValidity();
  }
}
