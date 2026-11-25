import { NavbarService, type Navbar } from '~/services/Navbar'

export const PLAYGROUND_NAV: Navbar = {
  id: -1, nom: 'Playground', ordre: 2,
  contents: [{ id: -1, nom: 'Générer un projet', route: '/playground', fichier: null, ordre: 1, navbaridTNavbar: { id: -1 } }]
}


export function useNavigation() {
  const { data } = useAsyncData<Navbar[]>('navbar-content',
    () => NavbarService.GetNavbarWithAllContent().catch(() => []), { default: () => [] })
  const route = useRoute()

  const navbars = computed(() => {
   
    const fromDb = (data.value ?? []).filter(n =>
      n.nom.toLowerCase() !== 'playground' && !n.contents?.some(c => c.route === '/playground'))
    const list = [...fromDb]
    list.splice(Math.min(1, list.length), 0, PLAYGROUND_NAV)   
    return list
  })
  const current = computed(() =>
    navbars.value.find(n => n.contents?.some(c => route.path === c.route || route.path.startsWith(c.route + '/'))))

  function href(n: Navbar) {
    return n.contents?.[0]?.route || '/'
  }
  return { navbars, current, href }
}