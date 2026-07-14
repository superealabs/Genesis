export function formatParams(text, params = {}) {
    if (typeof text !== 'string') {
        return text;
    }
    return text.replace(
        /\{([^}]+)\}/g,
        (_, key) => {
            if (params[key] !== undefined) {
                return params[key];
            }
            return `{${key}}`;
        }
    );
}