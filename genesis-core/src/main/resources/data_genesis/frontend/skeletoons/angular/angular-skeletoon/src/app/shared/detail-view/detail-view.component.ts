import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { objectToRecord } from '../../../utilities/utilities';
import { ConfirmationBoxComponent } from '../confirmation-box.component/confirmation-box.component';
import { Language,LanguageService } from '../services/language/language.service';
import { MotherComponent } from '../mother-component/mother.component';
import { Router } from '@angular/router';
import { TranslateService } from '../services/language/translate.service';
import {buildFileSource, getGeneratedFileName, isImageContent} from '../file-utils';

@Component({
  selector: 'app-detail-view',
  standalone: true,
  imports: [CommonModule, ConfirmationBoxComponent],
  templateUrl: './detail-view.component.html',
  styleUrls: ['./detail-view.component.css']
})
export class DetailViewComponent extends MotherComponent implements OnChanges,OnInit {
  readonly buildFileSource = buildFileSource;
  readonly getGeneratedFileName = getGeneratedFileName;
  readonly isImageContent = isImageContent;
  @Input() object: any = {};
  @Input() fieldTypes: Record<string, string> = {};
  @Input() id?: string = "";
  @Input() deletfn?: (id: string) => void;
  @Input() LinkContext:string="";
  data: Record<string, any> = {};
  showConfirmation = false;

  constructor(private router: Router,protected override langService: LanguageService,public override translateService: TranslateService) {
    super(langService,translateService)
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['object'] && this.object) {
      this.data = objectToRecord(this.object);
    }
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

  cancelDelete = () => {
    this.showConfirmation = false;
  };

  isFileField(fieldName: string): boolean {
    const fieldType = (this.fieldTypes[fieldName] ?? '')
      .replace(/\s/g, '')
      .toLowerCase();
    return [
      'uint8array',
      'byte[]',
      'bytearray',
      'file'
    ].includes(fieldType);
  }

  get entries() {
    return Object.entries(this.data);
  }
}
