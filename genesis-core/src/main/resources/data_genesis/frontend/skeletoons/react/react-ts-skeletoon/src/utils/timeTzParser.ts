// src/utils/timeTzParser.ts
import dayjs, { Dayjs } from 'dayjs';
import utc from 'dayjs/plugin/utc';
dayjs.extend(utc);

export const parseTimeTz = (timeTz: string): Dayjs | null => {
    if (!timeTz) return null;
    // "03:04:00Z" -> on injecte une date fictive pour que dayjs comprenne
    const d = dayjs.utc(`1970-01-01T${timeTz}`); // <-- clé
    return d.isValid() ? d : null;
};

export const formatTimeTz = (d: Dayjs): string | null => {
    return d?.isValid() ? d.utc().format('HH:mm:ss') + 'Z' : null;
};
