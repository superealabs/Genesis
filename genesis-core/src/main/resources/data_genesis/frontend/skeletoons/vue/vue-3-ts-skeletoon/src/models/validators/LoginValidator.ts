import type { ValidationRule } from '@/models/validators/ValidationRule.ts'
import { validators } from '@/utils/validators.ts'

export class LoginValidator {
  static usernameRules: ValidationRule[] = [
    {
      message: 'Username is required',
      validate: (value: string) => {
        return validators.required(value)
      },
    },
    {
      message: 'Username must be at least 3 characters',
      validate: (value: string) => {
        return validators.minLength(value, 3)
      },
    },
    {
      message: 'Username must be at most 20 characters',
      validate: (value: string) => {
        return validators.maxLength(value, 20)
      },
    },
    {
      message: 'Username can only contain letters, numbers, and underscores',
      validate: (value: string) => {
        return validators.pattern(value, /^[a-zA-Z0-9_]+$/)
      },
    },
  ]

  static passwordRules: ValidationRule[] = [
    {
      message: 'Password is required',
      validate: (value: string) => {
        return validators.required(value)
      },
    },
    {
      message: 'Password must be at least 4 characters',
      validate: (value: string) => {
        return validators.minLength(value, 4)
      },
    },
    {
      message: 'Password must be at most 100 characters',
      validate: (value: string) => {
        return validators.maxLength(value, 100)
      },
    },
  ]
}
