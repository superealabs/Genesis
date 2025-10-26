import type { ValidationRule } from '@/models/validators/ValidationRule.ts'

function required(value: any): boolean {
  return value !== null && value !== undefined && value !== ''
}

function minLength(value: string, length: number): boolean {
  return value.length >= length
}

function maxLength(value: string, length: number): boolean {
  return value.length <= length
}

function pattern(value: string, regex: RegExp): boolean {
  return regex.test(value)
}

function minValue(value: number, min: number): boolean {
  return value >= min
}

function maxValue(value: number, max: number): boolean {
  return value <= max
}

function checkRules(value: string, rules: ValidationRule[]) {
  for (const rule of rules) {
    if (!rule.validate(value)) {
      return rule.message
    }
  }
  return undefined
}

export const validators = {
  required,
  minLength,
  maxLength,
  pattern,
  minValue,
  maxValue,
  checkRules,
}
