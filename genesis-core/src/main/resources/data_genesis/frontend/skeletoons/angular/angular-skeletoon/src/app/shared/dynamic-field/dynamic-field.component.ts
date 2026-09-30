import { Component, HostListener, Input, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, AbstractControl, ValidatorFn, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; // pour *ngIf et *ngFor
import { buildFileSource, downloadFile, fileToBase64, getFileSize, getFileTypeLabel, getGeneratedFileName, isImageContent } from '../file-utils';

export interface FieldConfig {
  type: 'text' | 'number' | 'date' | 'select' | 'textarea' | 'file'| 'hidden' | 'datetime-local' | 'time' | 'checkbox';
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
  readonly buildFileSource = buildFileSource;
  readonly getGeneratedFileName = getGeneratedFileName;
  readonly isImageContent = isImageContent;
  readonly getFileSize = getFileSize;
  readonly getFileTypeLabel = getFileTypeLabel;
  previewSource: string | null = null;
  previewAlt = '';
  downloadedFile: string | null = null;
  private downloadFeedbackTimeout?: ReturnType<typeof setTimeout>;
  @Input() field!: FieldConfig;
  @Input() form!: FormGroup;
  @Input() initialData: any = {};
  @Input() validationErrors:any={};

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

    let value = this.initialData?.[this.field.name];
    let defaultValue: any = this.field.type === 'file' ? null : '';

    if (value !== undefined) {
      if (this.field.type === 'file') {
        defaultValue = value;
      } else if (typeof value === 'object' && value !== null) {
        defaultValue = JSON.stringify(value);
      } else {
        defaultValue = String(value); // ou value.toString()
      }
    }


    this.form.addControl(this.field.name, new FormControl(defaultValue, validators));
  }

  removeFile(): void {
    const control = this.getControl();
    control.setValue(null);
    control.markAsDirty();
    control.markAsTouched();
    control.updateValueAndValidity();
  }

  get currentFile(): unknown {
    return this.getControl()?.value;
  }

  openPreview(content: unknown): void {
    this.previewSource = buildFileSource(content);
    this.previewAlt = getGeneratedFileName(content);
  }

  closePreview(): void {
    this.previewSource = null;
  }

  downloadAsset(content: unknown): void {
    const fileName = getGeneratedFileName(content);
    downloadFile(content, fileName);
    this.downloadedFile = fileName;
    if (this.downloadFeedbackTimeout) clearTimeout(this.downloadFeedbackTimeout);
    this.downloadFeedbackTimeout = setTimeout(() => this.downloadedFile = null, 1600);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closePreview();
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
  async onFileChange(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const control = this.form.get(this.field.name);
    if (!control) {
      return;
    }
    if (!file) {
      control.setValue(null);
      control.updateValueAndValidity();
      return;
    }
    try {
      const base64 = await fileToBase64(file);
      control.setValue(base64);
      control.markAsDirty();
      control.markAsTouched();
      control.updateValueAndValidity();
    } catch (error) {
      console.error('Error while reading the selected file:', error);
      control.setValue(null);
      control.setErrors({
        ...control.errors,
        fileRead: true
      });
    }
  }
}
