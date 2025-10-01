import { Component,OnInit  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateService } from '../services/language/translate.service';
import { LanguageService } from '../services/language/language.service';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template:"",
})
export class MotherComponent implements OnInit 
{
  content:any;
  language:string="en";
  ngOnInit() {
      this.langService.language$.subscribe(lang => {
        this.language= lang;
         this.translateService.getTranslation(lang).subscribe(translations => {
          this.content = translations;
          console.log(this.content);
        });
      });
    }
  
    constructor(protected langService: LanguageService,public translateService: TranslateService) {

    }
}
