// src/utils/formatDuration.ts
import { parse } from 'iso8601-duration';

const UNITS = {
    years:  'a',
    months: 'm',
    days:   'd',
    hours:  'h',
    minutes:'min',
    seconds:'s',
} as const;

export function formatDuration(iso?: string | null): string {
    if (!iso) return '-';

    try {
        const d = parse(iso);

        // 1. Tout convertir en secondes
        let totalSec = 0;

        if (d.years)   totalSec += d.years   * 365 * 24 * 3600;
        if (d.months)  totalSec += d.months  * 30  * 24 * 3600; // approximation
        if (d.days)    totalSec += d.days    * 24 * 3600;
        if (d.hours)   totalSec += d.hours   * 3600;
        if (d.minutes) totalSec += d.minutes * 60;
        if (d.seconds) totalSec += d.seconds;

        // 2. Répartir dans les plus grosses unités possibles
        const years   = Math.floor(totalSec / (365 * 24 * 3600));
        totalSec %= (365 * 24 * 3600);

        const months  = Math.floor(totalSec / (30 * 24 * 3600));
        totalSec %= (30 * 24 * 3600);

        const days    = Math.floor(totalSec / (24 * 3600));
        totalSec %= (24 * 3600);

        const hours   = Math.floor(totalSec / 3600);
        totalSec %= 3600;

        const minutes = Math.floor(totalSec / 60);
        const seconds = totalSec % 60;

        // 3. Construire le libellé
        const parts: string[] = [];
        if (years)   parts.push(`${years}${UNITS.years}`);
        if (months)  parts.push(`${months}${UNITS.months}`);
        if (days)    parts.push(`${days}${UNITS.days}`);
        if (hours)   parts.push(`${hours}${UNITS.hours}`);
        if (minutes) parts.push(`${minutes}${UNITS.minutes}`);
        if (seconds || parts.length === 0) {
            // on arrondit à 2 décimales si nécessaire
            const rounded = seconds % 1 === 0 ? String(seconds) : seconds.toFixed(2);
            parts.push(`${rounded}${UNITS.seconds}`);
        }

        return parts.join(' ');
    } catch {
        return iso;
    }
}