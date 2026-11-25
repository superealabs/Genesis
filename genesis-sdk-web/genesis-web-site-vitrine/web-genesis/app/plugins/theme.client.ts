// Si le cookie vient d'être recopié depuis localStorage (anciens visiteurs), on aligne l'état dessus
export default defineNuxtPlugin(() => {
  const { theme } = useTheme()
  const saved = useCookie<'dark' | 'light'>('genesis-theme').value
  if ((saved === 'light' || saved === 'dark') && saved !== theme.value) theme.value = saved
})
