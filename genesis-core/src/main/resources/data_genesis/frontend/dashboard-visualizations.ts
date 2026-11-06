export interface DashboardField {
  role: string;
  key: string;
  columnName: string;
  label: string;
}

type DashboardRow = Record<string, unknown>;

interface ChartInstance {
  destroy(): void;
  resize(): void;
}

interface LeafletPopup {
  bindPopup(content: HTMLElement): LeafletPopup;
  bindTooltip(content: string): LeafletPopup;
  addTo(map: LeafletMap): LeafletPopup;
}

interface LeafletMap {
  setView(point: [number, number], zoom: number): LeafletMap;
  fitBounds(points: [number, number][], options: { padding: [number, number] }): LeafletMap;
  invalidateSize(): LeafletMap;
  remove(): void;
}

interface LeafletLibrary {
  map(element: HTMLElement): LeafletMap;
  tileLayer(
    url: string,
    options: { maxZoom: number; attribution: string }
  ): { addTo(map: LeafletMap): void };
  marker(point: [number, number]): LeafletPopup;
  circleMarker(point: [number, number], options: { radius: number }): LeafletPopup;
}

declare global {
  interface Window {
    Chart?: new (element: HTMLCanvasElement, config: Record<string, unknown>) => ChartInstance;
    L?: LeafletLibrary;
  }
}

const colors = ['#2563eb', '#7c3aed', '#db2777', '#ea580c', '#059669', '#0891b2'];
let chartPromise: Promise<void> | undefined;
let leafletPromise: Promise<void> | undefined;
const maps = new Map<string, LeafletMap>();

function loadScript(id: string, source: string): Promise<void> {
  const existing = document.getElementById(id) as HTMLScriptElement | null;
  if (existing) {
    if (existing.dataset['loaded'] === 'true') return Promise.resolve();
    return new Promise((resolve, reject) => {
      existing.addEventListener('load', () => resolve(), { once: true });
      existing.addEventListener('error', () => reject(new Error('Unable to load dashboard library: ' + source)), { once: true });
    });
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.id = id;
    script.src = source;
    script.async = true;
    script.onload = () => {
      script.dataset['loaded'] = 'true';
      resolve();
    };
    script.onerror = () => reject(new Error('Unable to load dashboard library: ' + source));
    document.head.appendChild(script);
  });
}

function ensureChart(): Promise<void> {
  chartPromise ??= loadScript(
    'genesis-dashboard-chartjs',
    'https://cdn.jsdelivr.net/npm/chart.js@4.4.7/dist/chart.umd.min.js'
  );
  return chartPromise;
}

function ensureLeaflet(): Promise<void> {
  leafletPromise ??= (async () => {
    if (!document.getElementById('genesis-dashboard-leaflet-css')) {
      const stylesheet = document.createElement('link');
      stylesheet.id = 'genesis-dashboard-leaflet-css';
      stylesheet.rel = 'stylesheet';
      stylesheet.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(stylesheet);
    }
    await loadScript(
      'genesis-dashboard-leaflet',
      'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
    );
    if (!window.L) throw new Error('Leaflet did not initialize.');
  })();
  return leafletPromise;
}

function fieldFor(fields: DashboardField[], role: string, index = 0): DashboardField | undefined {
  return fields.filter(field => field.role === role)[index];
}

function numberValue(value: unknown): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function formatDashboardValue(
  value: unknown,
  options: Record<string, unknown>,
  prefixKey = 'valuePrefix',
  suffixKey = 'valueSuffix',
  decimalsKey = 'decimals'
): string {
  if (value == null) return '-';
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) return String(value);
  const decimals = Number(options[decimalsKey]);
  const precision = Number.isInteger(decimals) && decimals >= 0 ? decimals : 0;
  return String(options[prefixKey] || '')
    + new Intl.NumberFormat(undefined, {
      minimumFractionDigits: precision,
      maximumFractionDigits: precision
    }).format(numeric)
    + String(options[suffixKey] || '');
}

export function dashboardVisualizationMessage(
  type: string,
  rows: DashboardRow[],
  fields: DashboardField[]
): string | null {
  const normalizedType = type.toUpperCase();
  if (!rows.length) return 'No data available.';
  if (normalizedType === 'KPI' && !fieldFor(fields, 'MEASURE')) {
    return 'The selected fields do not match this visualization.';
  }
  if (normalizedType === 'TABLE'
    && (!fields.some(field => field.role === 'COLUMN') || !rows.length)) {
    return 'No data available.';
  }
  if (['BAR_VERTICAL', 'BAR_HORIZONTAL', 'LINE', 'PIE', 'DONUT'].includes(normalizedType)
    && (!fieldFor(fields, 'DIMENSION') || !fieldFor(fields, 'MEASURE'))) {
    return 'The selected fields do not match this visualization.';
  }
  if (normalizedType === 'SCATTER'
    && (!fieldFor(fields, 'VALUE', 0) || !fieldFor(fields, 'VALUE', 1))) {
    return 'The selected fields do not match this visualization.';
  }
  if (normalizedType === 'MAP'
    && (!fieldFor(fields, 'LATITUDE')
      || !fieldFor(fields, 'LONGITUDE')
      || !rows.some(row => {
        const latitude = Number(row[fieldFor(fields, 'LATITUDE')!.key]);
        const longitude = Number(row[fieldFor(fields, 'LONGITUDE')!.key]);
        return Number.isFinite(latitude) && latitude >= -90 && latitude <= 90
          && Number.isFinite(longitude) && longitude >= -180 && longitude <= 180;
      }))) {
    return 'No data available.';
  }
  if (!['KPI', 'TABLE', 'MAP', 'BAR_VERTICAL', 'BAR_HORIZONTAL', 'LINE', 'PIE', 'DONUT', 'SCATTER'].includes(normalizedType)) {
    return 'Unsupported visualization type: ' + normalizedType;
  }
  return null;
}

function chartConfig(
  type: string,
  rows: DashboardRow[],
  fields: DashboardField[],
  options: Record<string, unknown>
): Record<string, unknown> {
  const dimension = fieldFor(fields, 'DIMENSION');
  const measure = fieldFor(fields, 'MEASURE');
  const isPie = type === 'PIE' || type === 'DONUT';

  if (type === 'SCATTER') {
    const xValue = fieldFor(fields, 'VALUE', 0)!;
    const yValue = fieldFor(fields, 'VALUE', 1)!;
    return {
      type: 'scatter',
      data: {
        datasets: [{
          label: String(options['legendY'] || yValue.columnName || yValue.key),
          data: rows.map(row => ({ x: numberValue(row[xValue.key]), y: numberValue(row[yValue.key]) })),
          backgroundColor: colors[0]
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { title: { display: Boolean(options['legendX']), text: options['legendX'] } },
          y: { title: { display: Boolean(options['legendY']), text: options['legendY'] } }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: (context: { parsed: { x: number; y: number } }) => 'X: '
                + formatDashboardValue(context.parsed.x, options, 'valuePrefixX', 'valueSuffixX', 'decimalsX')
                + ' | Y: '
                + formatDashboardValue(context.parsed.y, options, 'valuePrefixY', 'valueSuffixY', 'decimalsY')
            }
          }
        }
      }
    };
  }

  const chartType = type === 'BAR_VERTICAL' || type === 'BAR_HORIZONTAL'
    ? 'bar'
    : type === 'LINE' ? 'line' : type === 'PIE' ? 'pie' : 'doughnut';
  const horizontal = type === 'BAR_HORIZONTAL';
  const config: Record<string, unknown> = {
    type: chartType,
    data: {
      labels: rows.map(row => row[dimension!.key] == null ? '' : String(row[dimension!.key])),
      datasets: [{
        label: String(options['legendY'] || measure!.columnName || measure!.key),
        data: rows.map(row => numberValue(row[measure!.key])),
        backgroundColor: isPie ? colors : colors[0],
        borderColor: isPie ? '#fff' : colors[0],
        borderWidth: isPie ? 1 : 2,
        fill: false
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: horizontal ? 'y' : 'x',
      plugins: {
        legend: {
          display: isPie || Boolean(options['legendY']),
          position: isPie ? 'bottom' : 'top',
          title: { display: Boolean(options['legendTitle']), text: options['legendTitle'] }
        },
        tooltip: {
          callbacks: {
            label: (context: { parsed: number | { x: number; y: number } }) => formatDashboardValue(
              isPie
                ? context.parsed
                : horizontal
                  ? (context.parsed as { x: number }).x
                  : (context.parsed as { y: number }).y,
              options
            )
          }
        }
      }
    }
  };
  if (!isPie) {
    (config['options'] as Record<string, unknown>)['scales'] = horizontal
      ? {
        x: {
          title: { display: Boolean(options['legendY']), text: options['legendY'] },
          ticks: { callback: (value: number) => formatDashboardValue(value, options) }
        },
        y: { title: { display: Boolean(options['legendX']), text: options['legendX'] } }
      }
      : {
        x: { title: { display: Boolean(options['legendX']), text: options['legendX'] } },
        y: {
          title: { display: Boolean(options['legendY']), text: options['legendY'] },
          ticks: { callback: (value: number) => formatDashboardValue(value, options) }
        }
      };
  }
  return config;
}

async function createChart(
  element: HTMLElement,
  type: string,
  rows: DashboardRow[],
  fields: DashboardField[],
  options: Record<string, unknown>
): Promise<() => void> {
  await ensureChart();
  if (!window.Chart) throw new Error('Chart.js did not initialize.');
  const chart = new window.Chart(element as HTMLCanvasElement, chartConfig(type, rows, fields, options));
  return () => chart.destroy();
}

async function createMap(
  element: HTMLElement,
  rows: DashboardRow[],
  fields: DashboardField[],
  options: Record<string, unknown>
): Promise<() => void> {
  const latitude = fieldFor(fields, 'LATITUDE')!;
  const longitude = fieldFor(fields, 'LONGITUDE')!;
  const points = rows.map(row => ({
    latitude: Number(row[latitude.key]),
    longitude: Number(row[longitude.key]),
    row
  })).filter(point => Number.isFinite(point.latitude)
    && Number.isFinite(point.longitude)
    && point.latitude >= -90 && point.latitude <= 90
    && point.longitude >= -180 && point.longitude <= 180);
  if (!points.length) return () => {};

  await ensureLeaflet();
  const leaflet = window.L!;
  const map = leaflet.map(element);
  if (element.id) maps.set(element.id, map);
  leaflet.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);
  const bounds: [number, number][] = [];
  points.forEach(point => {
    const location: [number, number] = [point.latitude, point.longitude];
    bounds.push(location);
    const marker = String(options['markerType']).toUpperCase() === 'BUFFER'
      ? leaflet.circleMarker(location, { radius: 8 })
      : leaflet.marker(location);
    marker.addTo(map);
    const labelField = fieldFor(fields, 'COLUMN');
    const valueField = fieldFor(fields, 'MEASURE');
    const label = labelField ? point.row[labelField.key] : null;
    const value = valueField ? point.row[valueField.key] : null;
    const popup = document.createElement('div');
    if (label != null && String(label)) {
      const strong = document.createElement('strong');
      strong.textContent = String(label);
      popup.appendChild(strong);
    }
    if (value != null) {
      const valueText = document.createElement('div');
      valueText.textContent = formatDashboardValue(value, options);
      popup.appendChild(valueText);
    }
    if (popup.childNodes.length) marker.bindPopup(popup);
    if (label != null && String(label)) marker.bindTooltip(String(label));
  });
  if (bounds.length === 1) map.setView(bounds[0], 13);
  else map.fitBounds(bounds, { padding: [20, 20] });
  window.setTimeout(() => map.invalidateSize(), 0);
  return () => {
    if (element.id) maps.delete(element.id);
    map.remove();
  };
}

export function initializeDashboardVisualization(
  element: HTMLElement,
  type: string,
  rows: DashboardRow[],
  fields: DashboardField[],
  options: Record<string, unknown>
): Promise<() => void> {
  const normalizedType = type.toUpperCase();
  if (normalizedType === 'MAP') return createMap(element, rows, fields, options);
  return createChart(element, normalizedType, rows, fields, options);
}

export function resizeDashboardVisualization(elementId: string): void {
  maps.get(elementId)?.invalidateSize();
}
