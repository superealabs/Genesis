import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { FormGroup, FormControl, Validators, AbstractControl, ValidatorFn, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; // pour *ngIf et *ngFor
import { fileToBase64 } from '../file-utils';
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
import { CKEditorModule } from '@ckeditor/ckeditor5-angular';
import {
  Bold,
  ClassicEditor,
  Essentials,
  Heading,
  Italic,
  Link,
  List,
  Paragraph
} from 'ckeditor5';

export interface FieldConfig {
  type: string;
  name: string;
  label: string;
  options?: { value: any; label: string }[];
  min?: number;
  max?: number;
  step?: number;
  currency?: string;
  accept?: string;
  latitude?: string;
  longitude?: string;
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
  imports: [CommonModule, ReactiveFormsModule, CKEditorModule],
  templateUrl: './dynamic-field.component.html',
  styleUrls: ['./dynamic-field.component.css']
})
export class DynamicFieldComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {
  @Input() field!: FieldConfig;
  @Input() form!: FormGroup;
  @Input() initialData: any = {};
  @Input() validationErrors:any={};
  @ViewChild('mapElement') mapElement?: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private marker?: L.CircleMarker;
  private coordinateChanges?: Subscription;
  private registeredForm?: FormGroup;
  private destroyed = false;
  private registeredFieldName?: string;

  readonly editor = ClassicEditor;
  readonly editorConfig = {
    licenseKey: 'GPL',
    plugins: [Essentials, Paragraph, Bold, Italic, Link, List, Heading],
    toolbar: ['undo', 'redo', '|', 'heading', '|', 'bold', 'italic', 'link', '|', 'bulletedList', 'numberedList']
  };

  ngOnInit(): void {
    this.registerControl();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['form'] || changes['field'] || changes['initialData']) && this.form && this.field) {
      this.registerControl();
      if (changes['form'] && this.map) {
        this.bindCoordinateChanges();
        this.syncMapFromForm();
      }
    }
  }

  private registerControl(): void {
    if (this.registeredForm === this.form && this.registeredFieldName === this.field.name) {
      return;
    }
    if (this.registeredForm && this.registeredFieldName) {
      this.registeredForm.removeControl(this.registeredFieldName);
    }

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
    if (this.field.type === 'json-editor') validators.push(this.jsonValidator);

    let value = this.initialData?.[this.field.name];
    let defaultValue: any = this.field.type === 'color' ? '#000000' : '';

    if (value !== undefined) {
      if (this.field.type === 'map') {
        defaultValue = value;
      } else if (typeof value === 'object' && value !== null) {
        defaultValue = JSON.stringify(value);
      } else {
        defaultValue = String(value);
      }
    }


    this.form.addControl(this.field.name, new FormControl(defaultValue, validators));
    this.registeredForm = this.form;
    this.registeredFieldName = this.field.name;
  }

  ngAfterViewInit(): void {
    if (this.field.type !== 'map' || !this.mapElement) {
      return;
    }

    queueMicrotask(() => {
      if (this.destroyed || !this.mapElement) {
        return;
      }

      this.initializeMap();
    });
  }

  private initializeMap(): void {
    if (!this.mapElement) {
      return;
    }

    const initialPosition = this.getMapPosition();
    this.map = L.map(this.mapElement.nativeElement).setView(initialPosition, 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.map);
    this.marker = L.circleMarker(initialPosition, { radius: 7 }).addTo(this.map);
    this.map.on('click', event => this.setMapPosition(event.latlng.lat, event.latlng.lng));

    this.bindCoordinateChanges();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.coordinateChanges?.unsubscribe();
    this.map?.remove();
  }

  getNativeInputType(): string {
    if (this.field.type === 'phone') return 'tel';
    if (this.field.type === 'quantity' || this.field.type === 'currency') return 'number';
    return this.field.type;
  }

  formatJson(pretty: boolean): void {
    const control = this.getControl();
    const value = control.value;
    if (typeof value !== 'string' || value.trim() === '') {
      return;
    }
    try {
      const parsed = JSON.parse(value);
      control.setValue(JSON.stringify(parsed, null, pretty ? 2 : 0));
      control.markAsDirty();
      control.markAsTouched();
    } catch {
      control.markAsTouched();
      control.updateValueAndValidity();
    }
  }

  getControlValue(): any {
    return this.getControl().value;
  }

  getControl(): FormControl {
    return this.form.get(this.field.name) as FormControl;
  }

  private getCoordinateControls(): FormControl[] | null {
    if (!this.field.latitude || !this.field.longitude) {
      return null;
    }
    const latitude = this.form.get(this.field.latitude);
    const longitude = this.form.get(this.field.longitude);
    return latitude instanceof FormControl && longitude instanceof FormControl
      ? [latitude, longitude]
      : null;
  }

  private bindCoordinateChanges(): void {
    this.coordinateChanges?.unsubscribe();
    const coordinateControls = this.getCoordinateControls();
    if (!coordinateControls) {
      return;
    }
    this.coordinateChanges = new Subscription();
    coordinateControls.forEach(control => {
      this.coordinateChanges?.add(control.valueChanges.subscribe(() => this.syncMapFromForm()));
    });
  }

  private getMapPosition(): L.LatLngExpression {
    const coordinateControls = this.getCoordinateControls();
    if (coordinateControls) {
      const latitude = this.toCoordinate(coordinateControls[0].value);
      const longitude = this.toCoordinate(coordinateControls[1].value);
      if (latitude !== null && longitude !== null) {
        return [latitude, longitude];
      }
    }

    const value = this.getControl().value;
    const point = typeof value === 'string' ? this.parseMapValue(value) : value;
    const latitude = this.toCoordinate(point?.latitude ?? point?.lat);
    const longitude = this.toCoordinate(point?.longitude ?? point?.lng);
    return latitude !== null && longitude !== null
      ? [latitude, longitude]
      : [-18.8792, 47.5079];
  }

  private toCoordinate(value: unknown): number | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }
    const coordinate = Number(value);
    return Number.isFinite(coordinate) ? coordinate : null;
  }

  private parseMapValue(value: string): any {
    try {
      return JSON.parse(value);
    } catch {
      return null;
    }
  }

  private setMapPosition(latitude: number, longitude: number): void {
    const coordinateControls = this.getCoordinateControls();
    if (coordinateControls) {
      coordinateControls[0].setValue(latitude);
      coordinateControls[1].setValue(longitude);
      coordinateControls[0].markAsDirty();
      coordinateControls[1].markAsDirty();
    } else {
      this.getControl().setValue({ latitude, longitude });
      this.getControl().markAsDirty();
      this.getControl().markAsTouched();
    }
    this.marker?.setLatLng([latitude, longitude]);
  }

  private syncMapFromForm(): void {
    const position = this.getMapPosition();
    this.marker?.setLatLng(position);
    this.map?.setView(position);
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

  jsonValidator(control: AbstractControl) {
    if (control.value === null || control.value === undefined || control.value === '') {
      return null;
    }
    try {
      JSON.parse(control.value);
      return null;
    } catch {
      return { invalidJson: true };
    }
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
