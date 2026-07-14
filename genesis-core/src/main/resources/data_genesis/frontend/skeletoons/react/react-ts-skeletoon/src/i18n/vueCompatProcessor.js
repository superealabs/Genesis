import { createResolver } from './vueResolver';
import { formatParams } from './formatter';
import { resolvePlural } from './plural';

let resolver;

export function initVueCompat(i18n){
    resolver = createResolver(i18n);
}

const vueCompatProcessor = {
    name:'vueCompat',
    type:'postProcessor',

    process(value, key, options = {}){
        if(!resolver){
            return value;
        }
        let result = resolver(value, 0, options);
        result = resolvePlural(result, options.count);
        result = formatParams(result, options);
        return result;
    }
};
export default vueCompatProcessor;