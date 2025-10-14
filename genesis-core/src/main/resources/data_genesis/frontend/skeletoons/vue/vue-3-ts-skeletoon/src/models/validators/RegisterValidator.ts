import type { ValidationRule } from '@/models/validators/ValidationRule.ts'
import { validators } from '@/utils/validators.ts'

export class RegisterValidator {
  static usernameRules: ValidationRule[] = [
    {
      message: 'Username is required',
      validate: (value: string) => validators.required(value),
    },
    {
      message: 'Username must be at least 3 characters',
      validate: (value: string) => validators.minLength(value, 3),
    },
    {
      message: 'Username must be at most 20 characters',
      validate: (value: string) => validators.maxLength(value, 20),
    },
    {
      message: 'Username can only contain letters, numbers, and underscores',
      validate: (value: string) => validators.pattern(value, /^[a-zA-Z0-9_]+$/),
    },
  ]

  static emailRules: ValidationRule[] = [
    {
      message: 'Email is required',
      validate: (value: string) => validators.required(value),
    },
    {
      message: 'Invalid email address',
      validate: (value: string) =>
        validators.pattern(value, /^[^\s@]+@[^\s@]+\.[^\s@]+$/),
    },
  ]

  static passwordRules: ValidationRule[] = [
    {
      message: 'Password is required',
      validate: (value: string) => validators.required(value),
    },
    {
      message: 'Password must be at least 6 characters',
      validate: (value: string) => validators.minLength(value, 6),
    },
    {
      message: 'Password must be at most 100 characters',
      validate: (value: string) => validators.maxLength(value, 100),
    },
    {
      message: 'Password must contain at least one number',
      validate: (value: string) => validators.pattern(value, /\d/),
    },
    {
      message: 'Password must contain at least one letter',
      validate: (value: string) => validators.pattern(value, /[a-zA-Z]/),
    },
  ]

  static confirmPasswordRules: ValidationRule[] = [
    {
      message: 'Password confirmation is required',
      validate: (value: string) => validators.required(value),
    },
  ]
}
