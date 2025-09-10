import { Component, Input } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Language,LanguageService } from '../services/language/language.service'


@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() entities: { [key: string]: string } = {};
  @Input() views: { [key: string]: string } = {};
  logoPath: string="assets/icon/logo.jpg"
  logoAvailable = true;
  onLogoError() {
    this.logoAvailable = false;
  }

  selectedLanguage: Language = Language.EN;
  Language = Language;

  constructor(public langService: LanguageService) {}

  changeLanguage(lang: Language) {
    this.langService.setLanguage(lang);
  }
}
