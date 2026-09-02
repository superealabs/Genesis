import type { IR, VueLanguagePlugin } from '../types';
export declare const serviceScriptRE: RegExp;
export declare const tsCodegen: WeakMap<IR, {
    getScriptRanges: () => {
        bindings: import("../types").TextRange<import("typescript").Node>[];
        components: import("../types").TextRange<import("typescript").Node>[];
        exportDefault: (import("../types").TextRange<import("typescript").Node> & {
            expression: import("../types").TextRange<import("typescript").Expression>;
            isObjectLiteral: boolean;
            options?: {
                isObjectLiteral: boolean;
                expression: import("../types").TextRange;
                args: import("../types").TextRange<import("typescript").ObjectLiteralExpression>;
                components: import("../types").TextRange<import("typescript").ObjectLiteralExpression> | undefined;
                directives: import("../types").TextRange | undefined;
                name: import("../types").TextRange<import("typescript").StringLiteral> | undefined;
                inheritAttrs: string | undefined;
            };
        }) | undefined;
    } | undefined;
    getScriptSetupRanges: () => {
        bindings: import("../types").TextRange<import("typescript").Node>[];
        components: import("../types").TextRange<import("typescript").Node>[];
        leadingCommentEndOffset: number;
        importSectionEndOffset: number;
        defineModel: import("../parsers/scriptSetupRanges").DefineModel[];
        defineProps: import("../parsers/scriptSetupRanges").DefineProps | undefined;
        withDefaults: import("../parsers/scriptSetupRanges").CallExpressionRange | undefined;
        defineEmits: import("../parsers/scriptSetupRanges").DefineEmits | undefined;
        defineSlots: import("../parsers/scriptSetupRanges").DefineSlots | undefined;
        defineExpose: import("../parsers/scriptSetupRanges").CallExpressionRange | undefined;
        defineOptions: import("../parsers/scriptSetupRanges").DefineOptions | undefined;
        useAttrs: import("../parsers/scriptSetupRanges").CallExpressionRange[];
        useCssModule: import("../parsers/scriptSetupRanges").CallExpressionRange[];
        useSlots: import("../parsers/scriptSetupRanges").CallExpressionRange[];
        useTemplateRef: import("../parsers/scriptSetupRanges").UseTemplateRef[];
    } | undefined;
    getGeneratedScript: () => {
        generatedTypes: Set<string>;
        localTypes: {
            generate: () => Generator<string, void, unknown>;
            readonly PrettifyLocal: string;
            readonly WithDefaults: string;
            readonly WithSlots: string;
            readonly PropsChildren: string;
            readonly TypePropsToOption: string;
            readonly OmitIndexSignature: string;
        };
        inlayHints: import("../codegen/inlayHints").InlayHintInfo[];
        codes: import("../types").Code[];
    };
    getGeneratedTemplate: () => {
        getCommentInfo: () => {
            ignoreError?: boolean;
            expectError?: {
                token: number;
                node: import("@vue/compiler-dom").CommentNode;
            };
            generic?: {
                content: string;
                offset: number;
            };
        };
        enter: (node: import("@vue/compiler-dom").RootNode | import("@vue/compiler-dom").TemplateChildNode | import("@vue/compiler-dom").SimpleExpressionNode) => boolean;
        exit: () => Generator<import("../types").Code>;
        resolveCodeFeatures: (features: import("../types").VueCodeInformation) => import("../types").VueCodeInformation;
        getInternalVariable: () => string;
        scopes: {
            add(value: string): /*elided*/ any;
            clear(): void;
            declare(...variables: string[]): void;
            end(): Generator<import("../types").Code, any, any>;
            delete(value: string): boolean;
            forEach(callbackfn: (value: string, value2: string, set: Set<string>) => void, thisArg?: any): void;
            has(value: string): boolean;
            readonly size: number;
            [Symbol.iterator](): SetIterator<string>;
            entries(): SetIterator<[string, string]>;
            keys(): SetIterator<string>;
            values(): SetIterator<string>;
            readonly [Symbol.toStringTag]: string;
        }[];
        scope: () => {
            add(value: string): any;
            clear(): void;
            declare(...variables: string[]): void;
            end(): Generator<import("../types").Code, any, any>;
            delete(value: string): boolean;
            forEach(callbackfn: (value: string, value2: string, set: Set<string>) => void, thisArg?: any): void;
            has(value: string): boolean;
            readonly size: number;
            [Symbol.iterator](): SetIterator<string>;
            entries(): SetIterator<[string, string]>;
            keys(): SetIterator<string>;
            values(): SetIterator<string>;
            readonly [Symbol.toStringTag]: string;
        };
        contextAccesses: Map<string, Map<string, Set<number>>>;
        accessVariable: (source: string, name: string, offset?: number) => void;
        generateAutoImport: () => Generator<import("../types").Code>;
        conditions: string[];
        generateConditionGuards: () => Generator<string, void, unknown>;
        hoistVars: Map<string, string>;
        getHoistVariable: (originalVar: string) => string;
        generateHoistVariables: () => Generator<string, void, unknown>;
        templateRefs: Map<string, {
            typeExp: string;
            offset: number;
        }[]>;
        addTemplateRef: (name: string, typeExp: string, offset: number) => void;
        components: (() => string)[];
        dollarVars: Set<string>;
        inlayHints: import("../codegen/inlayHints").InlayHintInfo[];
        generatedTypes: Set<string>;
        inheritedAttrVars: Set<string>;
        singleRootElTypes: Set<string>;
        singleRootNodes: Set<import("@vue/compiler-dom").ElementNode | null>;
        slots: {
            name: string;
            offset?: number;
            tagRange: [number, number];
            propsVar: string;
        }[];
        dynamicSlots: {
            expVar: string;
            propsVar: string;
        }[];
        inVFor: boolean;
        codes: import("../types").Code[];
    } | undefined;
    getImportedComponents: () => Set<string>;
    getSetupBindings: () => Set<string>;
    getSetupExposed: () => Set<string>;
}>;
declare const plugin: VueLanguagePlugin;
export default plugin;
