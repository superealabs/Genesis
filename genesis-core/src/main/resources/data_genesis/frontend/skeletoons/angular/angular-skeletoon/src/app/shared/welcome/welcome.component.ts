import { Component,OnInit  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateService } from '../services/language/translate.service';
import { LanguageService } from '../services/language/language.service';
import { MotherComponent } from '../mother-component/mother.component';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent extends MotherComponent implements OnInit 
{}
