export interface ValidationRule {
  validate: (value: string) => boolean
  message: string
}
