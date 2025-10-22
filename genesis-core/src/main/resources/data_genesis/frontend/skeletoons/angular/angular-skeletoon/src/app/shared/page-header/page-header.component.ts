import { Component, Input, OnInit } from '@angular/core';
import { LinkButtonComponent } from '../link-button/link-button.component';
import { MotherComponent } from '../mother-component/mother.component';
import { NgIf } from '@angular/common';

function toKebabCase(str: string): string {
  return str
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2') 
    .replace(/[\s_]+/g, '-')               
    .toLowerCase();
}

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [LinkButtonComponent, NgIf],
  template: `
    <div class="page-header">
      <div class="page-title">
        <span class="light-name">{{ name }}</span>
        <span class="light-name"> / </span>
        <span class="bold-list">{{ contextValue}}</span>
      </div>
      <app-link-button
        *ngIf="!isView"
        [links]="linkMap"
        type="add">
      </app-link-button>
      <app-link-button
        *ngIf="isDetail"
        [links]="backList"
        type="back">
      </app-link-button>
      <app-link-button
        *ngIf="isForm"
        [links]="backList"
        type="back">
      </app-link-button>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin: 1.5rem 0 1rem;
    }

    .light-name, .bold-list {
      font-size: 1.1rem;
    }

    .light-name {
      font-weight: 700;
      font-size: 1.4rem;
    }

    .bold-list {
      font-weight: 700;
      color: #6b7280;
      font-size: 1.4rem;
    }
  `]
})
export class PageHeaderComponent extends MotherComponent implements OnInit{
  @Input() name: string = '';
  @Input() isView: boolean = false;
  @Input() isDetail: boolean = false;
  @Input() isForm: boolean = false;
  @Input() context: string = 'List';

  get contextValue(): string
  {
      if(this.context=="List")
      {
          return this.content.header.list;
      }
      else if(this.context=="Details")
        {
          return this.content.header.details
        }
      else if(this.context=="Form")
        {
          return this.content.header.form
        }  
        return "";
  }
  get singularLowercase(): string {
    return this.name.endsWith('s') ?
      this.name.slice(0, -1).toLowerCase() :
      this.name.toLowerCase();
  }
  
  
  get backList(): Record<string, string> {
    return {
      [this.content.header.backList]: `/${toKebabCase(this.name)}`
    };
  }

  get linkMap(): Record<string, string> {
    return {
      [this.content.header.add +" "+this.singularLowercase]: `/${toKebabCase(this.name)}/add`
    };
  }
}
