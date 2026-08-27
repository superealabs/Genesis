<template>
    <div
        class="inline-flex flex-col gap-1"
        :class="[layoutClasses, fillWidthClasses]"
    >
        <!-- ═══ Label ═══ -->
        <label
            v-if="label"
            class="text-sm font-medium text-muted"
            :class="labelClasses"
        >
            {{ label }}<span v-if="isMandatory" class="text-accent ml-0.5">*</span>
        </label>

        <!-- ═══ CAS BOOLEAN ═══ -->
        <template v-if="type === 'boolean'">
            <div class="inline-flex items-center gap-2" :class="containerSizeClasses">
                <input
                    type="checkbox"
                    class="w-4 h-4 rounded border-secondary bg-transparent text-accent
                           focus:ring-accent cursor-pointer disabled:cursor-not-allowed"
                    :checked="Boolean(modelValue)"
                    :disabled="disabled"
                    v-bind="$attrs"
                    @change="$emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
                />
            </div>
        </template>

        <!-- ═══ CAS STANDARD ═══ -->
        <template v-else>
            <div class="inline-flex items-center gap-1" :class="inputWrapperClasses">

                <span v-if="hasOuterLeftSlot" class="flex items-center flex-shrink-0">
                    <slot name="outer-left" />
                </span>

                <div
                    class="inline-flex items-center flex-1
                           border transition-all duration-200
                           focus-within:ring-1 focus-within:ring-accent"
                    :class="[
                        containerSizeClasses,
                        containerShapeClasses,
                        containerVariantClasses,
                        { 'opacity-50': disabled }
                    ]"
                >
                    <span
                        v-if="hasLeftSlot"
                        class="flex items-center flex-shrink-0 text-muted"
                        :class="slotPaddingClasses"
                    >
                        <slot name="left" />
                    </span>

                    <input
                        class="flex-1 min-w-0 bg-transparent outline-none text-text
                               placeholder:text-muted disabled:cursor-not-allowed"
                        :class="[inputSizeClasses, inputPaddingClasses]"
                        :disabled="disabled"
                        :placeholder="placeholder"
                        :type="type"
                        :value="modelValue"
                        v-bind="$attrs"
                        @input="handleInput"
                    />

                    <span
                        v-if="hasRightSlot"
                        class="flex items-center flex-shrink-0 text-muted"
                        :class="slotPaddingClasses"
                    >
                        <slot name="right" />
                    </span>
                </div>

                <span v-if="hasOuterRightSlot" class="flex items-center flex-shrink-0">
                    <slot name="outer-right" />
                </span>
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue';

export type InputType = 'text' | 'password' | 'number' | 'date' | 'boolean';

interface Props {
    modelValue?: string | number | boolean;
    placeholder?: string;
    type?: InputType;
    disabled?: boolean;
    variant?: 'primary' | 'secondary';
    shape?: 'rectangle' | 'pill';
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
    fillWidth?: boolean;
    label?: string;
    isMandatory?: boolean;
    oneLine?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    modelValue: '',
    placeholder: '',
    type: 'text',
    disabled: false,
    variant: 'primary',
    shape: 'rectangle',
    size: 'md',
    fillWidth: false,
    label: '',
    isMandatory: false,
    oneLine: false
});

const emit = defineEmits<{
    (e: 'update:modelValue', value: string | number | boolean): void;
}>();

defineOptions({ inheritAttrs: false });

const slots = useSlots();
const hasLeftSlot       = computed(() => !!slots.left);
const hasRightSlot      = computed(() => !!slots.right);
const hasOuterLeftSlot  = computed(() => !!slots['outer-left']);
const hasOuterRightSlot = computed(() => !!slots['outer-right']);

function handleInput(event: Event) {
    const target = event.target as HTMLInputElement;
    emit('update:modelValue', props.type === 'number' && target.value !== ''
        ? Number(target.value)
        : target.value
    );
}

// ── Taille du texte + SVG dans les slots (identique à GenesisButton) ──
const inputSizeClasses = computed(() => ({
    xs:   'text-[10px] [&_svg]:!w-3.5 [&_svg]:!h-3.5',
    sm:   'text-xs     [&_svg]:!w-4   [&_svg]:!h-4',
    md:   'text-sm     [&_svg]:!w-5   [&_svg]:!h-5',
    lg:   'text-base   [&_svg]:!w-6   [&_svg]:!h-6',
    xl:   'text-lg     [&_svg]:!w-7   [&_svg]:!h-7',
    '2xl':'text-xl     [&_svg]:!w-8   [&_svg]:!h-8',
}[props.size]));

// ── Padding vertical du container = convention GenesisButton rectangle ──
// ── Padding horizontal de l'input selon présence des slots ──
const inputPaddingClasses = computed(() => {
    const left  = hasLeftSlot.value;
    const right = hasRightSlot.value;

    // px réduit si slot présent de ce côté
    const pxLeft  = left  ? '' : ({ xs: 'pl-2', sm: 'pl-3', md: 'pl-4', lg: 'pl-5', xl: 'pl-6', '2xl': 'pl-7' })[props.size];
    const pxRight = right ? '' : ({ xs: 'pr-2', sm: 'pr-3', md: 'pr-4', lg: 'pr-5', xl: 'pr-6', '2xl': 'pr-7' })[props.size];

    return `${pxLeft} ${pxRight}`.trim();
});

// ── Padding des slots intérieurs ──
const slotPaddingClasses = computed(() => ({
    xs:   'px-1.5',
    sm:   'px-2',
    md:   'px-2.5',
    lg:   'px-3',
    xl:   'px-3.5',
    '2xl':'px-4',
}[props.size]));

// ── Container : py + h-fit comme GenesisButton, pas de h-N fixe ──
const containerSizeClasses = computed(() => ({
    xs:   'h-fit py-0.5',
    sm:   'h-fit py-1',
    md:   'h-fit py-1.5',
    lg:   'h-fit py-2',
    xl:   'h-fit py-2.5',
    '2xl':'h-fit py-3',
}[props.size]));

const containerShapeClasses = computed(() => ({
    rectangle: 'rounded',
    pill:      'rounded-full',
}[props.shape]));

const containerVariantClasses = computed(() => ({
    primary:   'bg-transparent border-secondary hover:border-accent',
    secondary: 'bg-transparent border-secondary hover:border-secondary',
}[props.variant]));

const fillWidthClasses  = computed(() => props.fillWidth ? 'w-full' : 'w-fit');

const layoutClasses = computed(() =>
    props.oneLine ? 'flex-row items-center' : 'flex-col'
);

const labelClasses = computed(() =>
    props.oneLine ? 'whitespace-nowrap' : ''
);

const inputWrapperClasses = computed(() => {
    if (props.fillWidth && props.oneLine) return 'flex-1 min-w-0';
    return props.fillWidth ? 'w-full' : 'w-fit';
});
</script>