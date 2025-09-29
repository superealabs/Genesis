import { Observable, of } from 'rxjs';
import english from '../../../../lang/english';
import french from '../../../../lang/french';
import { Injectable } from '@angular/core';


@Injectable({
  providedIn: 'root'
})
export class TranslateService  {
  getTranslation(lang: string): Observable<any> {
    switch (lang) {
      case 'fr':
        return of(french.messages); 
      case 'en':
        return of(english.messages);
      default:
        return of(english.messages);
    }
  }
}
