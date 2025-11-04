import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export enum Language
{
  EN = 'en',
  FR = 'fr',
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

  formatValue(value: any): string {
    if (value == null) return '';

    if (typeof value === 'number') {
      return this.formatNumber(value);
    }
    else if (value instanceof Date || this.isDateString(value)) {
      return this.formatDate(value);
    }
    else if (typeof value === 'string') {
      return value;
    }
    else {
      return String(value);
    }
  }

  formatDateTime(value: Date | string | number): string {
    if (!value) return '';
    const date = new Date(value);
    return new Intl.DateTimeFormat(this.currentLanguage, {
      year:'numeric', month:'2-digit', day:'2-digit',
      hour:'2-digit', minute:'2-digit', second:'2-digit'
    }).format(date);
  }

  formatTime(value: Date | string | number): string {
    if (!value) return '';
    const date = new Date(value);
    return new Intl.DateTimeFormat(this.currentLanguage, { hour:'2-digit', minute:'2-digit', second:'2-digit' }).format(date);
  }

  private isDateString(value: any): boolean {
    return typeof value === 'string' && !isNaN(Date.parse(value));
  }

  formatNumber(value: number): string {
    if (value == null) return '';
    return new Intl.NumberFormat(this.currentLanguage).format(value);
  }

  formatDate(value: Date | string | number): string {
    if (!value) return '';
    const date = new Date(value);
    const hasTime = date.getHours() !== 0 || date.getMinutes() !== 0 || date.getSeconds() !== 0;
    if (hasTime) {
      return new Intl.DateTimeFormat(this.currentLanguage, {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).format(date);
    } else {
      return new Intl.DateTimeFormat(this.currentLanguage, {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }).format(date);
    }
  }

  makeItReadable(value: string): string {
    if (!value) return '';
    let readable = value.replace(/[_\-]+/g, ' ');

    const idPattern = /^([a-zA-Z]+)id([A-Z][a-zA-Z]*)$/;
    const match = readable.match(idPattern);
    if (match) {
      readable = match[1];
    } else {
      readable = readable.replace(/([a-z])([A-Z])/g, '$1 $2');
    }
    readable = readable.charAt(0).toUpperCase() + readable.slice(1).trim();

    return readable;
  }


}
