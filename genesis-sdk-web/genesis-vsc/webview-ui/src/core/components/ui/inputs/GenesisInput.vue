<template>
    <div
        class="inline-flex flex-col gap-1"
        :class="[layoutClasses, fillWidthClasses]"
    >
        <!-- ═══ Label ═══ -->
        <label
            v-if="label && type !== 'boolean'"
            class="text-sm font-medium text-muted"
            :class="labelClasses"
        >
            {{ label }}<span v-if="isMandatory" class="text-accent ml-0.5">*</span>
        </label>

        <!-- ═══ CAS BOOLEAN ═══ -->
        <template v-if="type === 'boolean'">
            <GenesisSwitch
                :modelValue="Boolean(modelValue)"
                @update:modelValue="$emit('update:modelValue', $event)"
                :disabled="disabled"
                :size="switchSize"
                :label="label"
                v-bind="$attrs"
            />
        </template>

        <!-- ═══ CAS SELECT ═══ -->
        <template v-else-if="type === 'select'">
            <GenesisDropdown
                :trigger-variant="variant === 'secondary' ? 'secondary' : 'primary'"
                :trigger-size="size"
                :trigger-disabled="disabled"
                match-trigger-width
                :class="inputWrapperClasses"
            >
                <template #trigger>
                    <span class="truncate">
                        {{ modelValue || placeholder || 'Sélectionner...' }}
                    </span>
                </template>
                <slot />
            </GenesisDropdown>
        </template>

        <!-- ═══ CAS STANDARD (text, password, number, date, color, file, multiChoice) ═══ -->
        <template v-else>
            <div class="inline-flex flex-col gap-0" :class="inputWrapperClasses">

                <div class="inline-flex items-center gap-1" :class="inputWrapperClasses">

                    <!-- Slot gauche extérieur -->
                    <span v-if="hasOuterLeftSlot" class="flex items-center flex-shrink-0">
                        <slot name="outer-left" />
                    </span>

                    <!-- Container input -->
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
                            class="flex-1 min-w-0 bg-transparent outline-none text-text
                                   placeholder:text-muted disabled:cursor-not-allowed"
                            :class="[inputSizeClasses, inputPaddingClasses]"
                            :disabled="disabled"
                            :placeholder="placeholder"
                            :type="type === 'color' || type === 'file' ? 'text' : type"
                            :value="modelValue"
                            v-bind="$attrs"
                            @input="handleInput"
                        />

                        <!-- Slot droit intérieur -->
                        <span
                            class="flex items-center flex-shrink-0 text-muted"
                            :class="slotPaddingClasses"
                        >
                            <!-- Mode file : bouton dossier -->
                            <template v-if="type === 'file'">
                                <GenesisButtonIcon
                                    size="xs"
                                    variant="tertiary"
                                    :disabled="disabled"
                                    @click.stop="$emit('browse', accept)"
                                >
                                    <IconFolder />
                                </GenesisButtonIcon>
                            </template>

                            <!-- Mode color : roue chromatique -->
                            <template v-else-if="type === 'color'">
                                <input
                                    type="color"
                                    class="w-6 h-6 p-0 border-0 rounded cursor-pointer bg-transparent"
                                    :value="String(modelValue)"
                                    :disabled="disabled"
                                    @input="handleColorWheelInput"
                                />
                            </template>

                            <!-- Mode multiChoice : bouton ajouter -->
                            <template v-else-if="multiChoice">
                                <GenesisButtonIcon
                                    size="xs"
                                    variant="tertiary"
                                    :disabled="!modelValue"
                                    @click.stop="handleAddChoice"
                                >
                                    <IconPlus />
                                </GenesisButtonIcon>
                            </template>

                            <!-- Slot droit custom sinon -->
                            <template v-else-if="hasRightSlot">
                                <slot name="right" />
                            </template>
                        </span>
                    </div>

                    <!-- Slot droit extérieur -->
                    <span v-if="hasOuterRightSlot" class="flex items-center flex-shrink-0">
                        <slot name="outer-right" />
                    </span>
                </div>

                <!-- Chips multiChoice -->
                <div
                    v-if="multiChoice && choices.length > 0"
                    class="flex flex-wrap gap-1.5 mt-1.5"
                >
                    <GenesisLabel
                        v-for="(choice, index) in choices"
                        :key="index"
                        :text="choice"
                        @remove="$emit('remove-choice', choice)"
                    />
                </div>
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue';
import GenesisSwitch from './GenesisSwitch.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisLabel from '@/core/components/ui/labels/GenesisLabel.vue';
import IconPlus from '@/core/components/ui/icons/IconPlus.vue';
import IconFolder from '@/core/components/ui/icons/IconFolder.vue';

export type InputType = 'text' | 'password' | 'number' | 'date' | 'boolean' | 'color' | 'select' | 'file';

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
    // ─ multiChoice ─
    multiChoice?: boolean;
    choices?: string[];
    // ─ file ─
    accept?: string;
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
    oneLine: false,
    multiChoice: false,
    choices: () => [],
    accept: '*',
});

const emit = defineEmits<{
    (e: 'update:modelValue', value: string | number | boolean): void;
    (e: 'add-choice', value: string): void;
    (e: 'remove-choice', value: string): void;
    (e: 'browse', accept: string): void;
}>();

defineOptions({ inheritAttrs: false });

const slots = useSlots();
const hasLeftSlot       = computed(() => !!slots.left);
const hasRightSlot      = computed(() => !!slots.right);
const hasOuterLeftSlot  = computed(() => !!slots['outer-left']);
const hasOuterRightSlot = computed(() => !!slots['outer-right']);

// ═══ Handlers ═══

function handleInput(event: Event) {
    const target = event.target as HTMLInputElement;
    emit('update:modelValue', props.type === 'number' && target.value !== ''
        ? Number(target.value)
        : target.value
    );
}

function handleColorWheelInput(event: Event) {
    const target = event.target as HTMLInputElement;
    emit('update:modelValue', target.value);
}

function handleAddChoice() {
    if (!props.modelValue) return;
    emit('add-choice', String(props.modelValue));
    emit('update:modelValue', '');
}

// ═══ Classes ═══

const hasRightContent = computed(() =>
    hasRightSlot.value || props.multiChoice || props.type === 'color' || props.type === 'file'
);

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
    const right = hasRightContent.value;

    const pxLeft  = left  ? '' : ({ xs: 'pl-2', sm: 'pl-3', md: 'pl-4', lg: 'pl-5', xl: 'pl-6', '2xl': 'pl-7' })[props.size];
    const pxRight = right ? '' : ({ xs: 'pr-2', sm: 'pr-3', md: 'pr-4', lg: 'pr-5', xl: 'pr-6', '2xl': 'pr-7' })[props.size];

    return `${pxLeft} ${pxRight}`.trim();
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
    secondary: 'bg-transparent border-bg-light hover:border-secondary',
}[props.variant]));

const fillWidthClasses = computed(() => props.fillWidth ? 'w-full' : 'w-fit');

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

const switchSize = computed(() => {
    if (props.size === 'xs' || props.size === 'sm') return 'sm';
    if (props.size === 'xl' || props.size === '2xl') return 'lg';
    return 'md';
});
</script>