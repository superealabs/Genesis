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

        <!-- ═══ CAS BOOLEAN (Checkbox / Toggle) ═══ -->
        <template v-if="type === 'boolean'">
            <div class="inline-flex items-center gap-2 h-8">
                <input
                    type="checkbox"
                    class="w-4 h-4 rounded border-secondary bg-transparent text-accent focus:ring-accent cursor-pointer disabled:cursor-not-allowed"
                    :checked="Boolean(modelValue)"
                    :disabled="disabled"
                    v-bind="$attrs"
                    @change="$emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
                />
            </div>
        </template>

        <!-- ═══ CAS STANDARD (text, number, date, password, etc.) ═══ -->
        <template v-else>
            <div class="inline-flex items-center gap-1" :class="inputWrapperClasses">

                <!-- Slot gauche extérieur -->
                <span v-if="hasOuterLeftSlot" class="flex items-center flex-shrink-0">
                    <slot name="outer-left" />
                </span>

                <!-- Container input -->
                <div
                    class="inline-flex items-center flex-1 border transition-all duration-200 focus-within:ring-1 focus-within:ring-accent py-4"
                    :class="[
                        containerSizeClasses,
                        containerShapeClasses,
                        containerVariantClasses,
                        { 'opacity-50': disabled }
                    ]"
                >
                    <!-- Slot gauche intérieur -->
                    <span
                        v-if="hasLeftSlot"
                        class="flex items-center flex-shrink-0 text-muted"
                        :class="slotPaddingClasses"
                    >
                        <slot name="left" />
                    </span>

                    <!-- Input natif -->
                    <input
                        class="flex-1 min-w-0 bg-transparent outline-none text-text placeholder:text-muted disabled:cursor-not-allowed"
                        :class="[inputSizeClasses, inputPaddingClasses]"
                        :disabled="disabled"
                        :placeholder="placeholder"
                        :type="type"
                        :value="modelValue"
                        v-bind="$attrs"
                        @input="handleInput"
                    />

                    <!-- Slot droit intérieur -->
                    <span
                        v-if="hasRightSlot"
                        class="flex items-center flex-shrink-0 text-muted"
                        :class="slotPaddingClasses"
                    >
                        <slot name="right" />
                    </span>
                </div>

                <!-- Slot droit extérieur -->
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
    if (props.type === 'number') {
        emit('update:modelValue', target.value !== '' ? Number(target.value) : '');
    } else {
        emit('update:modelValue', target.value);
    }
}

const inputSizeClasses = computed(() => ({
    xs:   'text-[10px] [&_svg]:!w-3.5 [&_svg]:!h-3.5',
    sm:   'text-xs     [&_svg]:!w-4   [&_svg]:!h-4',
    md:   'text-sm     [&_svg]:!w-5   [&_svg]:!h-5',
    lg:   'text-base   [&_svg]:!w-6   [&_svg]:!h-6',
    xl:   'text-lg     [&_svg]:!w-7   [&_svg]:!h-7',
    '2xl':'text-xl     [&_svg]:!w-8   [&_svg]:!h-8',
}[props.size]));

const inputPaddingClasses = computed(() => {
    const left  = hasLeftSlot.value;
    const right = hasRightSlot.value;

    const config: Record<string, Record<string, string>> = {
        xs:   { both: 'px-1',     leftOnly: 'pl-1 pr-2',     rightOnly: 'pl-2 pr-1',     none: 'px-2   py-0.5' },
        sm:   { both: 'px-1.5',   leftOnly: 'pl-1.5 pr-3',   rightOnly: 'pl-3 pr-1.5',   none: 'px-3   py-1'   },
        md:   { both: 'px-2',     leftOnly: 'pl-2 pr-4',     rightOnly: 'pl-4 pr-2',     none: 'px-4   py-1.5' },
        lg:   { both: 'px-2.5',   leftOnly: 'pl-2.5 pr-5',   rightOnly: 'pl-5 pr-2.5',   none: 'px-5   py-2'   },
        xl:   { both: 'px-3',     leftOnly: 'pl-3 pr-6',     rightOnly: 'pl-6 pr-3',     none: 'px-6   py-2.5' },
        '2xl':{ both: 'px-3.5',   leftOnly: 'pl-3.5 pr-7',   rightOnly: 'pl-7 pr-3.5',   none: 'px-7   py-3'   },
    };

    const row = config[props.size];
    if (left && right) return row.both;
    if (left)          return row.leftOnly;
    if (right)         return row.rightOnly;
    return row.none;
});

const slotPaddingClasses = computed(() => ({
    xs:   'px-1.5',
    sm:   'px-2',
    md:   'px-2.5',
    lg:   'px-3',
    xl:   'px-3.5',
    '2xl':'px-4',
}[props.size]));

const containerSizeClasses = computed(() => ({
    xs:   'h-6',
    sm:   'h-7',
    md:   'h-8',
    lg:   'h-9',
    xl:   'h-10',
    '2xl':'h-11',
}[props.size]));

const containerShapeClasses = computed(() => ({
    rectangle: 'rounded',
    pill:      'rounded-full',
}[props.shape]));

const containerVariantClasses = computed(() => ({
    primary:   'bg-transparent border-secondary hover:border-accent',
    secondary: 'bg-transparent border-secondary hover:border-secondary',
}[props.variant]));

const fillWidthClasses = computed(() => props.fillWidth ? 'w-full' : 'w-fit');

const layoutClasses = computed(() => {
    return props.oneLine ? 'flex-row items-center gap-2' : 'flex-col gap-1';
});

const labelClasses = computed(() => {
    return props.oneLine ? 'mr-2 whitespace-nowrap' : 'mb-1';
});

const inputWrapperClasses = computed(() => {
    if (props.fillWidth && props.oneLine) return 'flex-1';
    return props.fillWidth ? 'w-full' : 'w-fit';
});
</script>