import { Component, Input } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Language, LanguageService } from '../services/language/language.service';

@Component({
  selector: 'app-banner',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './banner.component.html',
  styleUrls: ['./banner.component.css']
})
export class BannerComponent {
  @Input() title: string = "YourApp";
  @Input() logoPath: string = "assets/icon/logo.jpg";

  logoAvailable = true;
  selectedLanguage: Language = Language.EN;
  Language = Language;

  constructor(public langService: LanguageService) {}

  onLogoError() {
    this.logoAvailable = false;
  }

  changeLanguage(lang: Language) {
    this.langService.setLanguage(lang);
    this.selectedLanguage = lang;
  }
}
