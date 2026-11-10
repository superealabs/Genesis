import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-detail-map',
  standalone: true,
  template: '<div #mapElement class="detail-map" role="img" aria-label="Location map"></div>',
  styles: [':host { display: block; width: 100%; } .detail-map { width: 100%; height: 280px; border: 1px solid #ccc; border-radius: 6px; overflow: hidden; }']
})
export class DetailMapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() latitude: number | string | null = null;
  @Input() longitude: number | string | null = null;
  @ViewChild('mapElement') mapElement?: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private marker?: L.CircleMarker;

  ngAfterViewInit(): void { this.renderMap(); }

  ngOnChanges(_changes: SimpleChanges): void {
    if (this.map) this.renderMap();
  }

  ngOnDestroy(): void { this.map?.remove(); }

  private renderMap(): void {
    if (!this.mapElement) return;
    const lat = Number(this.latitude);
    const lng = Number(this.longitude);
    const hasPosition = this.latitude !== null && this.latitude !== '' && this.longitude !== null && this.longitude !== ''
      && Number.isFinite(lat) && Number.isFinite(lng);
    const center: L.LatLngExpression = hasPosition ? [lat, lng] : [0, 0];
    if (!this.map) {
      this.map = L.map(this.mapElement.nativeElement, { zoomControl: true, scrollWheelZoom: false, dragging: true }).setView(center, hasPosition ? 13 : 2);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '&copy; OpenStreetMap contributors', maxZoom: 19 }).addTo(this.map);
    } else {
      this.map.setView(center, hasPosition ? 13 : 2);
    }
    this.marker?.remove();
    this.marker = hasPosition ? L.circleMarker(center as L.LatLngExpression, { radius: 7 }).addTo(this.map) : undefined;
    setTimeout(() => this.map?.invalidateSize(), 0);
  }
}
