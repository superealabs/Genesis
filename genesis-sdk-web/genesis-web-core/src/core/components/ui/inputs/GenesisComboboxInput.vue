<template>
    <div class="relative w-full">
        <Combobox v-slot="{ open }" v-model="internalSelected" @update:modelValue="handleComboboxSelect" nullable>
            <div class="relative">
                <div
                    class="relative w-full cursor-default overflow-hidden rounded transition-all duration-200"
                    :class="[containerSizeClasses, containerShapeClasses, containerVariantClasses, { 'opacity-50': disabled }]"
                >
                    <div class="flex h-full pl-2 items-center">
                        <IconSearch />
                        <ComboboxInput
                            class="w-full h-full bg-transparent outline-none text-text placeholder:text-muted disabled:cursor-not-allowed pl-0"
                            :class="inputPaddingClasses"
                            :disabled="disabled"
                            :placeholder="placeholder"
                            :displayValue="(item: any) => item ? getOptionLabel(item) : (multiChoice ? comboboxQuery : '')"
                            @change="handleComboboxChange"
                            @click="openIfClosed(open)"
                        />
                    </div>
                    <ComboboxButton ref="comboboxButtonRef" class="absolute inset-y-0 right-0 flex items-center pr-2 text-muted">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="h-5 w-5" aria-hidden="true">
                            <path fill-rule="evenodd" d="M10 3a.75.75 0 01.55.24l3.25 3.5a.75.75 0 11-1.1 1.02L10 4.852 7.3 7.76a.75.75 0 01-1.1-1.02l3.25-3.5A.75.75 0 0110 3zm-3.76 9.2a.75.75 0 011.06.04l2.7 2.908 2.7-2.908a.75.75 0 111.1 1.02l-3.25 3.5a.75.75 0 01-1.1 0l-3.25-3.5a.75.75 0 01.04-1.06z" clip-rule="evenodd" />
                        </svg>
                    </ComboboxButton>
                </div>
                
                <TransitionRoot leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0" @after-leave="comboboxQuery = ''">
                    <ComboboxOptions class="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-md py-1 text-base sm:text-sm shadow-lg" :class="fieldMenuClasses">
                        <div v-if="filteredOptions.length === 0 && comboboxQuery !== ''" class="relative cursor-default select-none px-4 py-2 text-text-muted">
                            Aucun résultat trouvé.
                        </div>
                        <ComboboxOption v-for="option in filteredOptions" as="template" :key="getOptionValue(option)" :value="option" v-slot="{ selected, active }">
                            <li class="relative cursor-default select-none py-2 pl-3 pr-9" :class="{ 'bg-bg-secondary text-secondary': active, 'text-text': !active }">
                                <span class="block truncate" :class="{ 'font-medium': selected, 'font-normal': !selected }">
                                    {{ getOptionLabel(option) }}
                                </span>
                                <span v-if="selected" class="absolute inset-y-0 right-0 flex items-center pr-4" :class="{ 'text-accent': active, 'text-muted': !active }">
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

        <div v-if="multiChoice && choices && choices.length > 0" class="flex flex-wrap gap-1.5 mt-1.5">
            <GenesisLabel v-for="(choice, index) in choices" :key="index" :text="choice" @remove="$emit('remove-choice', choice)" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Combobox, ComboboxInput, ComboboxButton, ComboboxOptions, ComboboxOption, TransitionRoot } from '@headlessui/vue';
import IconSearch from '../icons/IconSearch.vue';
import GenesisLabel from '@genesis-labs/web-core/core/components/ui/labels/GenesisLabel.vue';
import { CONTROL_SIZES, FIELD_VARIANTS, type UI_Variant, type UI_Size_Unit } from '@genesis-labs/web-core/core/config/ui.config';

const props = withDefaults(defineProps<{
    modelValue?: string | number | string[];
    placeholder?: string;
    disabled?: boolean;
    variant?: UI_Variant;
    shape?: 'rectangle' | 'pill';
    size?: UI_Size_Unit;
    options?: { label: string, value: any }[];
    multiChoice?: boolean;
    choices?: string[];
}>(), {
    modelValue: undefined,
    placeholder: '',
    disabled: false,
    variant: 'neutral',
    shape: 'rectangle',
    size: 'md',
    options: () => [],
    multiChoice: false,
    choices: () => [],
});

const emit = defineEmits<{
    (e: 'update:modelValue', value: any): void;
    (e: 'add-choice', value: string): void;
    (e: 'remove-choice', value: string): void;
}>();

const comboboxQuery = ref('');
const internalSelected = ref<any>(null);

const tokens = computed(() => CONTROL_SIZES[props.size]);
const containerSizeClasses = computed(() => `${tokens.value.box} ${tokens.value.text} ${tokens.value.icon}`);
const inputPaddingClasses = computed(() => tokens.value.pl);
const containerShapeClasses = computed(() => ({ rectangle: 'rounded', pill: 'rounded-full' }[props.shape]));

const fieldTokens = computed(() => FIELD_VARIANTS[props.variant] ?? FIELD_VARIANTS.neutral);
const containerVariantClasses = computed(() => fieldTokens.value.container);
const fieldMenuClasses = computed(() => fieldTokens.value.menu);

const comboboxButtonRef = ref<{ $el?: HTMLElement } | null>(null);

function openIfClosed(open: boolean) {
    if (!open && !props.disabled) comboboxButtonRef.value?.$el?.click();
}

watch(() => props.modelValue, (newVal) => {
    if (!props.multiChoice) {
        internalSelected.value = props.options?.find(opt => getOptionValue(opt) === newVal) ?? newVal;
    }
}, { immediate: true });

const filteredOptions = computed(() => {
    if (!props.options || props.options.length === 0) return [];
    if (comboboxQuery.value === '') return props.options;
    const query = comboboxQuery.value.toLowerCase().replace(/\s+/g, '');
    return props.options.filter((option) => getOptionLabel(option).toLowerCase().replace(/\s+/g, '').includes(query));
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
        emit('add-choice', getOptionLabel(option));
        comboboxQuery.value = '';
        internalSelected.value = null;
    } else {
        emit('update:modelValue', getOptionValue(option));
        internalSelected.value = option;
    }
}
</script>