import dayjs from 'dayjs'
import customParseFormat from 'dayjs/plugin/customParseFormat'

// On active le plugin une seule fois pour toute l'application
dayjs.extend(customParseFormat)

export function useDateFormat() {
  /**
   * Formate une date de manière sécurisée et intelligente
   * Détecte automatiquement s'il s'agit d'un LocalDate ou LocalDateTime
   * 
   * @param dateValue La date (string ISO, timestamp, ou objet Date)
   * @param formatOut Optionnel : Force un format spécifique ('date' ou 'datetime')
   * @returns La date formatée en string, ou '--/--/--' si invalide
   */
  const formatDate = (dateValue: any, formatOut?: 'date' | 'datetime' | string): string => {
    if (!dateValue) return '--/--/--'

    let parsedDate = dayjs(dateValue)

    // Fallback pour les formats personnalisés si le parsing natif échoue
    if (!parsedDate.isValid()) {
      parsedDate = dayjs(dateValue, 'DD/MM/YYYY HH:mm')
    }

    if (!parsedDate.isValid()) {
      return 'Format invalide'
    }

    // Détection automatique du type de date
    // Si l'utilisateur n'a pas forcé de format, on détecte automatiquement
    if (!formatOut) {
      // Si la string contient 'T' ou a plus de 10 caractères, c'est un datetime
      // Sinon, c'est une date simple (LocalDate)
      const dateString = String(dateValue)
      const isDateTime = dateString.includes('T') || dateString.length > 10
      formatOut = isDateTime ? 'DD/MM/YYYY HH:mm' : 'DD/MM/YYYY'
    } else if (formatOut === 'date') {
      formatOut = 'DD/MM/YYYY'
    } else if (formatOut === 'datetime') {
      formatOut = 'DD/MM/YYYY HH:mm'
    }

    return parsedDate.format(formatOut)
  }

  return {
    formatDate
  }
}