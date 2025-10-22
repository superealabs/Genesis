// src/utils/timeParser.ts
import dayjs, { Dayjs } from 'dayjs';

export const parseTimeString = (timeStr: string): Dayjs | null => {
    if (!timeStr) return null;

    // Format "HH:mm:ss"
    if (/^\d{2}:\d{2}:\d{2}$/.test(timeStr)) {
        const [hours, minutes, seconds] = timeStr.split(':').map(Number);
        return dayjs().set('hour', hours).set('minute', minutes).set('second', seconds);
    }

    // Format ISO ou autres
    try {
        const date = new Date(timeStr);
        if (!isNaN(date.getTime())) {
            return dayjs(date);
        }
    } catch {
        // ignore
    }

    return null;
};