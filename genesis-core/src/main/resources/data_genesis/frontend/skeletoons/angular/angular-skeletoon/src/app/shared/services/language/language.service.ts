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
    else if (value instanceof Date) {
      const hasTime = value.getHours() !== 0 || value.getMinutes() !== 0 || value.getSeconds() !== 0;
      return hasTime ? this.formatDateTime(value) : this.formatDateOnly(value);
    }
    else if (typeof value === 'string') {
      if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        return this.formatDateOnly(value);       // ← ISO date-only
      }
      else if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(value)) {
        return this.formatDateTime(value);        // ← ISO datetime
      }
      else if (/^\d{2}:\d{2}(:\d{2})?$/.test(value)) {
        return this.formatTime(value);            // ← Time seule
      }
      return value;                               // ← String classique
    }
    else {
      return String(value);
    }
  }

  /**
   * Date seule (sans heure). Gère le cas ISO string sans fuseau horaire.
   */
  private formatDateOnly(value: Date | string): string {
    if (!value) return '';

    if (typeof value === 'string') {
      // Parse local pour éviter le décalage UTC → local
      const [year, month, day] = value.split('-').map(Number);
      return new Intl.DateTimeFormat(this.currentLanguage, {
        year: 'numeric', month: '2-digit', day: '2-digit'
      }).format(new Date(year, month - 1, day));
    }

    // Objet Date natif
    return new Intl.DateTimeFormat(this.currentLanguage, {
      year: 'numeric', month: '2-digit', day: '2-digit'
    }).format(value);
  }

  formatDateTime(value: Date | string | number): string {
    if (!value) return '';
    return new Intl.DateTimeFormat(this.currentLanguage, {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    }).format(new Date(value));
  }

  formatTime(value: Date | string | number): string {
    if (!value) return '';
    return new Intl.DateTimeFormat(this.currentLanguage, {
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    }).format(new Date(value));
  }

  formatNumber(value: number): string {
    if (value == null) return '';
    return new Intl.NumberFormat(this.currentLanguage).format(value);
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