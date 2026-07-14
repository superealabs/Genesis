export function createResolver(i18n) {
    const modifiers = {
        capitalize(value) {
            if (!value) return '';
            return value.charAt(0).toUpperCase() + value.slice(1);
        },

        upper(value) {
            return value.toUpperCase();
        },

        lower(value) {
            return value.toLowerCase();
        }
    };


    function translate(key, options = {}) {
        // help resolving: common.password
        if (!key.startsWith('messages.'))
            key = `messages.${key}`;

        return i18n.t(key, {
            ...options,
            postProcess: false
        });
    }

    function resolve(text, depth = 0, options = {}) {
        // avoid infinite loop
        if (depth > 10) {
            return text;
        }

        if (typeof text !== 'string') {
            return text;
        }

        // Helper for Vue syntax: @:common.password
        text = text.replace(
            /@:([\w.]+)/g,
            (_, key) => {
                return resolve(
                    translate(key, options),
                    depth + 1
                );

            }
        );

        // Helper for Vue syntax: @.capitalize:home.greet
        text = text.replace(
            /@\.([a-zA-Z]+):([\w.]+)/g,
            (_, modifier, key)=>{
                const value =
                    resolve(
                        translate(key),
                        depth + 1
                    );

                if(modifiers[modifier]) {
                    return modifiers[modifier](value);
                }

                return value;
            }
        );
        return text;
    }


    return resolve;
}