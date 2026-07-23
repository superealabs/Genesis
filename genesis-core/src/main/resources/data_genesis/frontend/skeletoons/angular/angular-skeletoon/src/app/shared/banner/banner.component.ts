import { Component, Input, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Language, LanguageService } from '../services/language/language.service';
import { MotherComponent } from '../mother-component/mother.component';



@Component({
  selector: 'app-banner',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './banner.component.html',
  styleUrls: ['./banner.component.css']
})
export class BannerComponent extends MotherComponent implements OnInit{
  @Input() title: string = "YourApp";
  @Input() logoPath: string = "assets/icon/logo.jpg";

  logoAvailable = true;
  selectedLanguage: Language = this.langService.currentLanguage;
  Language = Language;

  override ngOnInit(): void {
    super.ngOnInit();
    this.langService.language$.subscribe(lang => {
      this.selectedLanguage = lang;
    });
  }

  onLogoError() {
    this.logoAvailable = false;
  }

  changeLanguage(lang: Language) {
    this.langService.setLanguage(lang);
    this.selectedLanguage = lang;
  }
}
