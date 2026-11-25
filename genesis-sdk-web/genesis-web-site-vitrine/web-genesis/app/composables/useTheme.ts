export type Theme = 'dark' | 'light'

export function useTheme() {
  // Pas de valeur par défaut : le cookie n'est écrit qu'au premier choix (absent = sombre)
  const cookie = useCookie<Theme | undefined>('genesis-theme', {
    maxAge: 60 * 60 * 24 * 365,
    sameSite: 'lax',
    path: '/'
  })
  const theme = useState<Theme>('theme', () => (cookie.value === 'light' ? 'light' : 'dark'))

  function apply(t: Theme) {
    theme.value = t
    cookie.value = t
    if (import.meta.client) {
      try { localStorage.setItem('genesis-theme', t) } catch { /* navigation privée */ }
    }
  }

  return { theme, apply }
}