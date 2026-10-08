<template>
    <div class="inline-flex flex-col gap-1" :class="[layoutClasses, fillWidthClasses, $attrs.class]">
        
        <label v-if="label && type !== 'boolean' && type !== 'checkbox-3-state'" class="text-sm font-medium text-muted" :class="labelClasses">
            {{ label }}<span v-if="isMandatory" class="text-primary ml-0.5">*</span>
        </label>

        <template v-if="type === 'boolean'">
            <GenesisSwitch :modelValue="Boolean(modelValue)" @update:modelValue="$emit('update:modelValue', $event)" :disabled="disabled" :size="switchSize" :label="label" v-bind="$attrs" />
        </template>

        <template v-else-if="type === 'checkbox-3-state'">
            <GenesisCheckbox :modelValue="(modelValue as CheckboxState) || 'neutral'" @update:modelValue="$emit('update:modelValue', $event)" :disabled="disabled" :size="checkboxSize" :label="label" />
        </template>

        <template v-else-if="type === 'select'">
            <GenesisComboboxInput v-if="useComboboxForSelect" v-bind="sharedSelectProps" v-on="choiceListeners" />
            <GenesisDropdownInput v-else v-bind="sharedSelectProps" v-on="choiceListeners">
                <template #default="{ close }"><slot :close="close" /></template>
            </GenesisDropdownInput>
        </template>

        <template v-else-if="type === 'combobox'">
            <GenesisComboboxInput v-bind="sharedSelectProps" />
        </template>

        <template v-else-if="type === 'textarea'">
            <GenesisTextareaInput v-bind="sharedTextareaProps" />
        </template>

        <!-- ✅ CAS STANDARD : Délégué proprement au nouveau composant -->
        <template v-else>
            <GenesisNativeInput 
                v-bind="sharedNativeProps"
                @update:model-value="(val) => $emit('update:modelValue', val)"
                @add-choice="(val) => $emit('add-choice', val)"
                @browse="(val) => $emit('browse', val)"
                @request-folder-path="$emit('request-folder-path')"
            >
                <template #outer-left><slot name="outer-left" /></template>
                <template #left><slot name="left" /></template>
                <template #right><slot name="right" /></template>
                <template #outer-right><slot name="outer-right" /></template>
            </GenesisNativeInput>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue';
import GenesisSwitch from './GenesisSwitch.vue';
import GenesisCheckbox, { type CheckboxState } from './GenesisCheckbox.vue';
import GenesisTextareaInput from './GenesisTextareaInput.vue';
import GenesisComboboxInput from './GenesisComboboxInput.vue';
import GenesisDropdownInput from './GenesisDropdownInput.vue';
import GenesisNativeInput from './GenesisNativeInput.vue'; // ✅ NOUVEAU IMPORT

import { type UI_Variant, type UI_Size_Unit, type InputType } from '@genesis-labs/web-core/core/config/ui.config';

interface Props {
    modelValue?: string | number | boolean | CheckboxState | string | string[];
    placeholder?: string;
    type?: InputType;
    disabled?: boolean;
    variant?: UI_Variant;
    shape?: 'rectangle' | 'pill';
    size?: UI_Size_Unit;
    fillWidth?: boolean;
    label?: string;
    isMandatory?: boolean;
    oneLine?: boolean;
    multiChoice?: boolean;
    choices?: string[];
    accept?: string;
    options?: { label: string, value: any }[];
}

const props = withDefaults(defineProps<Props>(), {
    modelValue: '', placeholder: '', type: 'text', disabled: false, variant: 'neutral',
    shape: 'rectangle', size: 'md', fillWidth: false, label: '', isMandatory: false,
    oneLine: false, multiChoice: false, choices: () => [], accept: '*', options: () => [],
});

const emit = defineEmits<{
    (e: 'update:modelValue', value: any): void;
    (e: 'add-choice', value: string): void;
    (e: 'remove-choice', value: string): void;
    (e: 'browse', accept: string): void;
    (e: 'request-folder-path'): void;
}>();

// Écouteurs communs aux champs délégués : sans eux, leurs événements ne remontent jamais
const valueListeners = {
    'update:modelValue': (value: any) => emit('update:modelValue', value),
};
const choiceListeners = {
    ...valueListeners,
    'remove-choice': (value: string) => emit('remove-choice', value),
};

defineOptions({ inheritAttrs: false });
useSlots(); // Conservé pour s'assurer que les slots sont enregistrés si besoin, bien que délégués

const MAXIMAL_DROPDOWN_OPTION = 7;
const useComboboxForSelect = computed(() => props.type === 'select' && props.options && props.options.length >= MAXIMAL_DROPDOWN_OPTION);

const fillWidthClasses = computed(() => props.fillWidth ? 'w-full' : 'w-fit');
const layoutClasses = computed(() => props.oneLine ? 'flex-row items-center' : 'flex-col');
const labelClasses = computed(() => props.oneLine ? 'whitespace-nowrap' : '');

const switchSize = computed(() => (props.size === 'xs' || props.size === 'sm') ? 'sm' : (props.size === 'xl' || props.size === '2xl') ? 'lg' : 'md');
const checkboxSize = computed(() => (props.size === 'xs' || props.size === 'sm') ? 'sm' : (props.size === 'xl' || props.size === '2xl' || props.size === 'lg') ? 'lg' : 'md');

// ✅ PROPS DÉLÉGUÉES PROPRES ET TYÉES
const sharedSelectProps = computed(() => ({
    modelValue: props.modelValue as string | number | string[] | undefined,
    placeholder: props.placeholder,
    disabled: props.disabled,
    variant: props.variant,
    shape: props.shape,
    size: props.size,
    options: props.options,
    multiChoice: props.multiChoice,
    choices: props.choices,
}));

const sharedTextareaProps = computed(() => ({
    modelValue: props.modelValue as string | number | undefined,
    placeholder: props.placeholder,
    disabled: props.disabled,
    variant: props.variant,
    shape: props.shape,
    size: props.size,
}));

const sharedNativeProps = computed(() => ({
    modelValue: props.modelValue as string | number | undefined,
    placeholder: props.placeholder,
    type: props.type,
    disabled: props.disabled,
    variant: props.variant,
    shape: props.shape,
    size: props.size,
    fillWidth: props.fillWidth,
    oneLine: props.oneLine,
    multiChoice: props.multiChoice,
    accept: props.accept,
}));
</script>