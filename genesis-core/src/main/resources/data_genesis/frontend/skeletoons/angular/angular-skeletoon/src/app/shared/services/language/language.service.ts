import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export enum Language {
  EN = 'en',
  FR = 'fr'
}

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private languageSubject = new BehaviorSubject<Language>(Language.EN);
  language$ = this.languageSubject.asObservable();

  setLanguage(lang: Language) {
    this.languageSubject.next(lang);
  }

  get currentLanguage(): Language {
    return this.languageSubject.value;
  }
}
