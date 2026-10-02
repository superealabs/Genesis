<template>
    <div
        class="inline-flex flex-col gap-1"
        :class="[layoutClasses, fillWidthClasses, $attrs.class]"
    >
        <!-- ═══ Label ═══ -->
        <label
            v-if="label && type !== 'boolean' && type !== 'checkbox-3-state'"
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

        <!-- ═══ CAS CHECKBOX 3 ÉTATS (Neutre, Checked, Refused) ═══ -->
        <template v-else-if="type === 'checkbox-3-state'">
            <GenesisCheckbox
                :modelValue="(modelValue as CheckboxState) || 'neutral'"
                @update:modelValue="$emit('update:modelValue', $event)"
                :disabled="disabled"
                :size="checkboxSize"
                :label="label"
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
                :align="'left'"
                :force-down="true"
            >
                <template #trigger>
                    <span class="truncate">
                        {{ modelValue || placeholder || 'Sélectionner...' }}
                    </span>
                </template>
                <template #default="{ close }">
                    <slot :close="close" />
                </template>
            </GenesisDropdown>

            <div v-if="multiChoice && choices && choices.length > 0" class="flex flex-wrap gap-1.5 mt-1.5">
                <GenesisLabel
                    v-for="(choice, index) in choices"
                    :key="index"
                    :text="choice"
                    @remove="$emit('remove-choice', choice)"
                />
            </div>
        </template>

        <!-- ═══ CAS COMBOBOX (Autocomplete avec recherche pour grandes listes) ═══ -->
        <template v-else-if="type === 'combobox'">
            <div class="relative w-full">
                <Combobox v-model="internalSelected" @update:modelValue="handleComboboxSelect" nullable>
                    <div class="relative">
                        <div
                            class="relative w-full cursor-default overflow-hidden rounded border transition-all duration-200 focus-within:ring-1 focus-within:ring-accent"
                            :class="[
                                containerSizeClasses,
                                containerShapeClasses,
                                containerVariantClasses,
                                { 'opacity-50': disabled }
                            ]"
                        >
                            <div class="flex pl-2 items-center">
                                <IconSearch />
                                <ComboboxInput
                                    class="w-full bg-transparent outline-none text-text placeholder:text-muted disabled:cursor-not-allowed pl-0"
                                    :class="[inputSizeClasses, inputPaddingClasses]"
                                    :disabled="disabled"
                                    :placeholder="placeholder"
                                    :displayValue="(item: any) => item ? getOptionLabel(item) : (multiChoice ? comboboxQuery : '')"
                                    @change="handleComboboxChange"
                                />
                            </div>
                            <ComboboxButton class="absolute inset-y-0 right-0 flex items-center pr-2 text-muted">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-5 w-5" aria-hidden="true">
                                    <path fill-rule="evenodd" d="M10 3a.75.75 0 01.55.24l3.25 3.5a.75.75 0 11-1.1 1.02L10 4.852 7.3 7.76a.75.75 0 01-1.1-1.02l3.25-3.5A.75.75 0 0110 3zm-3.76 9.2a.75.75 0 011.06.04l2.7 2.908 2.7-2.908a.75.75 0 111.1 1.02l-3.25 3.5a.75.75 0 01-1.1 0l-3.25-3.5a.75.75 0 01.04-1.06z" clip-rule="evenodd" />
                                </svg>
                            </ComboboxButton>
                        </div>
                        
                        <TransitionRoot
                            leave="transition ease-in duration-100"
                            leaveFrom="opacity-100"
                            leaveTo="opacity-0"
                            @after-leave="comboboxQuery = ''"
                        >
                            <ComboboxOptions
                                class="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-md bg-bg-light dark:bg-bg-dark border border-secondary py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm"
                            >
                                <div
                                    v-if="filteredOptions.length === 0 && comboboxQuery !== ''"
                                    class="relative cursor-default select-none px-4 py-2 text-text-muted"
                                >
                                    Aucun résultat trouvé.
                                </div>

                                <ComboboxOption
                                    v-for="option in filteredOptions"
                                    as="template"
                                    :key="getOptionValue(option)"
                                    :value="option"
                                    v-slot="{ selected, active }"
                                >
                                    <li
                                        class="relative cursor-default select-none py-2 pl-3 pr-9"
                                        :class="{
                                            'bg-accent/20 text-accent': active,
                                            'text-text': !active,
                                        }"
                                    >
                                        <span
                                            class="block truncate"
                                            :class="{ 'font-medium': selected, 'font-normal': !selected }"
                                        >
                                            {{ getOptionLabel(option) }}
                                        </span>
                                        <span
                                            v-if="selected"
                                            class="absolute inset-y-0 right-0 flex items-center pr-4"
                                            :class="{ 'text-accent': active, 'text-muted': !active }"
                                        >
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-5 w-5" aria-hidden="true">
                                                <path fill-rule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clip-rule="evenodd" />
                                            </svg>
                                        </span>
                                    </li>
                                </ComboboxOption>
                            </ComboboxOptions>
                        </TransitionRoot>
                    </div>
                </Combobox>

                <!-- Chips multiChoice -->
                <div v-if="multiChoice && choices && choices.length > 0" class="flex flex-wrap gap-1.5 mt-1.5">
                    <GenesisLabel
                        v-for="(choice, index) in choices"
                        :key="index"
                        :text="choice"
                        @remove="$emit('remove-choice', choice)"
                    />
                </div>
            </div>
        </template>

        <!-- ═══ CAS TEXTAREA (Multi-ligne redimensionnable) ═══ -->
        <template v-else-if="type === 'textarea'">
            <div
                class="flex flex-col min-h-0 overflow-hidden border border-secondary rounded-lg bg-bg-dark/30 relative transition-all duration-200 focus-within:ring-1 focus-within:ring-accent"
                :class="[containerVariantClasses, { 'opacity-50': disabled }]"
                :style="resizeStyle"
            >
                <textarea
                    class="flex-1 w-full min-h-0 bg-transparent outline-none text-text placeholder:text-muted disabled:cursor-not-allowed p-3 custom-scrollbar resize-none"
                    :disabled="disabled"
                    :placeholder="placeholder"
                    :value="String(modelValue ?? '')"
                    v-bind="$attrs"
                    @input="handleInput"
                />
                
                <!-- Handle de redimensionnement vertical -->
                <div 
                    class="absolute bottom-0 left-0 right-0 h-3 py-3 cursor-ns-resize flex items-center justify-center hover:bg-accent/20 transition-colors z-20 rounded-b-lg"
                    @mousedown="startResizeBottom"
                    title="Redimensionner verticalement"
                >
                    <IconDragY class="text-text-muted opacity-50 hover:opacity-100" :size="20" />
                </div>
            </div>
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
                        class="inline-flex items-center flex-1 border transition-all duration-200 focus-within:ring-1 focus-within:ring-accent"
                        :class="[
                            containerSizeClasses,
                            containerShapeClasses,
                            containerVariantClasses,
                            { 'opacity-50': disabled }
                        ]"
                    >
                        <!-- Slot gauche intérieur -->
                        <span v-if="hasLeftSlot" class="flex items-center flex-shrink-0 text-muted" :class="slotPaddingClasses">
                            <slot name="left" />
                        </span>

                        <!-- Input natif -->
                        <input
                            class="flex-1 min-w-0 bg-transparent outline-none text-text placeholder:text-muted disabled:cursor-not-allowed"
                            :class="[inputSizeClasses, inputPaddingClasses]"
                            :disabled="disabled"
                            :placeholder="placeholder"
                            :type="type === 'color' || type === 'file' ? 'text' : type"
                            :value="modelValue"
                            v-bind="$attrs"
                            @input="handleInput"
                        />

                        <!-- Slot droit intérieur -->
                        <span class="flex items-center flex-shrink-0 text-muted" :class="slotPaddingClasses">
                            <template v-if="type === 'file'">
                                <GenesisButtonIcon size="xs" variant="tertiary" :disabled="disabled" @click.stop="$emit('browse', accept)">
                                    <IconFolder />
                                </GenesisButtonIcon>
                            </template>

                            <template v-else-if="type === 'path'">
                                <GenesisButtonIcon size="xs" variant="tertiary" :disabled="disabled" @click.stop="$emit('request-folder-path')">
                                    <IconFolder />
                                </GenesisButtonIcon>
                            </template>

                            <template v-else-if="type === 'color'">
                                <input
                                    type="color"
                                    class="w-6 h-6 p-0 border-0 rounded cursor-pointer bg-transparent"
                                    :value="String(modelValue)"
                                    :disabled="disabled"
                                    @input="handleColorWheelInput"
                                />
                            </template>

                            <template v-else-if="multiChoice">
                                <GenesisButtonIcon size="xs" variant="tertiary" :disabled="!modelValue" @click.stop="handleAddChoice">
                                    <IconPlus />
                                </GenesisButtonIcon>
                            </template>

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
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, useSlots, ref, watch } from 'vue';
import {
  Combobox,
  ComboboxInput,
  ComboboxButton,
  ComboboxOptions,
  ComboboxOption,
  TransitionRoot,
} from '@headlessui/vue';

import GenesisSwitch from './GenesisSwitch.vue';
import GenesisCheckbox, { type CheckboxState } from './GenesisCheckbox.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisLabel from '@genesis-labs/web-core/core/components/ui/labels/GenesisLabel.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconFolder from '@genesis-labs/web-core/core/components/ui/icons/IconFolder.vue';
import IconSearch from '../icons/IconSearch.vue';
import { useResizable } from '@genesis-labs/web-core/core/composables/ux/useResizable.ts';
import IconDragY from '../icons/IconDragY.vue';

// ✅ AJOUT : 'combobox' au type
export type InputType = 'text' | 'password' | 'number' | 'date' | 'boolean' | 'color' | 'select' | 'file' | 'checkbox-3-state' | 'path' | 'textarea' | 'combobox';

interface Props {
    modelValue?: string | number | boolean | CheckboxState | string;
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
    // ✅ AJOUT : Options pour le combobox (ex: [{ label: 'Français', value: 'fr' }])
    options?: { label: string, value: any }[];
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
    options: () => [],
});

const emit = defineEmits<{
    (e: 'update:modelValue', value: string | number | boolean | CheckboxState): void;
    (e: 'add-choice', value: string): void;
    (e: 'remove-choice', value: string): void;
    (e: 'browse', accept: string): void;
    (e: 'request-folder-path'): void;
}>();

defineOptions({ inheritAttrs: false });

const slots = useSlots();
const hasLeftSlot       = computed(() => !!slots.left);
const hasRightSlot      = computed(() => !!slots.right);
const hasOuterLeftSlot  = computed(() => !!slots['outer-left']);
const hasOuterRightSlot = computed(() => !!slots['outer-right']);

const MIN_HEIGHT = 100;

// gestion du resizer
const { resizeStyle, startResizeBottom } = useResizable({
    minHeight: MIN_HEIGHT,
    maxHeight: () => MIN_HEIGHT * 2,
    resizableX: ref(false),
    resizableY: ref(true)
});

// ═══ Logique Combobox ═══
const comboboxQuery = ref('');
const internalSelected = ref<any>(null);

// Synchronise la sélection interne avec le modelValue pour le mode choix unique
watch(() => props.modelValue, (newVal) => {
    if (!props.multiChoice && props.type === 'combobox') {
        internalSelected.value = props.options?.find(opt => getOptionValue(opt) === newVal) || newVal;
    }
}, { immediate: true });

const filteredOptions = computed(() => {
    if (!props.options || props.options.length === 0) return [];
    if (comboboxQuery.value === '') return props.options;
    
    const query = comboboxQuery.value.toLowerCase().replace(/\s+/g, '');
    return props.options.filter((option) =>
        getOptionLabel(option).toLowerCase().replace(/\s+/g, '').includes(query)
    );
});

function getOptionLabel(option: any): string {
    return typeof option === 'string' ? option : (option.label || String(option));
}

function getOptionValue(option: any): any {
    return typeof option === 'string' ? option : (option.value !== undefined ? option.value : option);
}

function handleComboboxChange(event: Event) {
    const target = event.target as HTMLInputElement;
    comboboxQuery.value = target.value;
    if (!props.multiChoice) {
        internalSelected.value = target.value;
        emit('update:modelValue', target.value);
    }
}

function handleComboboxSelect(option: any) {
    if (props.multiChoice) {
        // En mode multiChoice, on émet l'ajout et on réinitialise la recherche
        emit('add-choice', getOptionLabel(option));
        comboboxQuery.value = '';
        internalSelected.value = null;
    } else {
        // En mode choix unique, on met à jour le modelValue
        emit('update:modelValue', getOptionValue(option));
        internalSelected.value = option;
    }
}

// ═══ Handlers Standards ═══
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
    hasRightSlot.value || props.multiChoice || props.type === 'color' || props.type === 'file' || props.type === 'path'
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
const layoutClasses = computed(() => props.oneLine ? 'flex-row items-center' : 'flex-col');
const labelClasses = computed(() => props.oneLine ? 'whitespace-nowrap' : '');

const inputWrapperClasses = computed(() => {
    if (props.fillWidth && props.oneLine) return 'flex-1 min-w-0';
    return props.fillWidth ? 'w-full' : 'w-fit';
});

const switchSize = computed(() => {
    if (props.size === 'xs' || props.size === 'sm') return 'sm';
    if (props.size === 'xl' || props.size === '2xl') return 'lg';
    return 'md';
});

const checkboxSize = computed(() => {
    if (props.size === 'xs' || props.size === 'sm') return 'sm';
    if (props.size === 'xl' || props.size === '2xl' || props.size === 'lg') return 'lg';
    return 'md';
});
</script>