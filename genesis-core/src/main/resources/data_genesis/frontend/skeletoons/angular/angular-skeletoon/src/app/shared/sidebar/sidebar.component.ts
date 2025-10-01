import { Component, Input, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MotherComponent } from '../mother-component/mother.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent extends MotherComponent implements OnInit{
  @Input() entities: { [key: string]: string } = {};
  @Input() views: { [key: string]: string } = {};
  logoPath: string="assets/icon/logo.jpg"
  logoAvailable = true;
  onLogoError() {
    this.logoAvailable = false;
  }
}
