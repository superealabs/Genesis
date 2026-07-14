export function resolvePlural(text, count) {
    if (typeof text !== 'string'
        || count === undefined
        || count === null) {
        return text;
    }


    // Convert format Vue I18n: "singular | plural"
    const parts = text.split('|')
        .map(v => v.trim());

    if (parts.length === 1)
        return text;

    if (count === 0 || count === 1)
        return parts[0];

    return parts[1];
}