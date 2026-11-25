<script setup lang="ts">
// Administration du site : CRUD via Siteback (t_equipe, t_navbar, t_content, t_charte_graphique)
// + fichiers envoyés (public/upload). t_content.fichier = chemin du fichier HTML de la page.
import { EquipeService, type Equipe } from '~/services/Equipe'
import { NavbarService, type Navbar } from '~/services/Navbar'
import { ContentService, HOME_FILE, type Content } from '~/services/Content'
import { CharteGraphiqueService, type CharteGraphique } from '~/services/CharteGraphique'
import { uploadFile } from '~/services/Upload'

useHead({ title: 'Administration · Genesis' })

type Tab = 'equipe' | 'navbar' | 'content' | 'charte' | 'accueil'
const TABS: { key: Tab; label: string }[] = [
  { key: 'content', label: 'Contenus (pages)' }, { key: 'navbar', label: 'Navbar' },
  { key: 'equipe', label: 'Équipe' }, { key: 'charte', label: 'Charte graphique' }, { key: 'accueil', label: 'Accueil' }
]
const tab = ref<Tab>('content')
const message = ref('')
const error = ref('')

const equipe = ref<Equipe[]>([])
const navbars = ref<Navbar[]>([])
const contents = ref<Content[]>([])
const chartes = ref<CharteGraphique[]>([])
const form = ref<any>(null)
const html = ref('')            // HTML du fichier de la page en cours d'édition
const editorMode = ref<'visual' | 'code'>('visual')   // éditeur par blocs (sans code) ou HTML
const routes = computed(() => [...new Set(contents.value.map(c => c.route).filter(Boolean))])

async function load() {
  error.value = ''
  try {
    if (tab.value === 'equipe') equipe.value = await EquipeService.Getall()
    if (tab.value === 'navbar' || tab.value === 'content') navbars.value = await NavbarService.Getall()
    if (tab.value === 'content') contents.value = await ContentService.Getall()
    if (tab.value === 'charte') chartes.value = await CharteGraphiqueService.Getall()
    if (tab.value === 'accueil') { form.value = { accueil: true }; html.value = await ContentService.ReadFile(HOME_FILE).catch(() => '') }
  } catch (e: any) { error.value = e.message }
}
watch(tab, () => { form.value = null; message.value = ''; load() }, { immediate: true })

async function edit(row?: any) {
  message.value = ''
  if (tab.value === 'equipe') form.value = row ? { ...row } : { nom: '', prenom: '', poste: '', email: '', photo: '', github: '', linkedin: '' }
  if (tab.value === 'navbar') form.value = row ? { ...row } : { nom: '', ordre: navbars.value.length + 1 }
  if (tab.value === 'charte') form.value = row ? { ...row } : { file: '', version: '', dateAjout: new Date().toISOString().slice(0, 10) }
  if (tab.value === 'content') {
    form.value = row
      ? { ...row, navbarId: row.navbaridTNavbar?.id }
      : { nom: '', route: '/', fichier: '', ordre: contents.value.length + 1, navbarId: navbars.value[0]?.id }
    html.value = row?.fichier ? await ContentService.ReadFile(row.fichier).catch(() => '') : ''
  }
}

function refreshSite() {
  refreshNuxtData()   // navbar, pages, équipe, charte
}

async function save() {
  error.value = ''; message.value = ''
  try {
    const f = form.value
    if (tab.value === 'equipe') await EquipeService.Set(f)
    if (tab.value === 'navbar') await NavbarService.Set(f)
    if (tab.value === 'charte') await CharteGraphiqueService.Set(f)
    if (tab.value === 'content') {
      // 1. le texte de la page est écrit dans un fichier (public/upload/navbar/...)
      if (f.route !== '/playground' && (html.value || f.fichier)) {
        f.fichier = await ContentService.WriteFile(f.route.replace(/^\/|\/$/g, '').replace(/\//g, '-') || 'page', html.value, f.fichier)
      }
      // 2. le chemin du fichier est enregistré dans t_content.fichier
      await ContentService.Set({ ...f, navbaridTNavbar: { id: Number(f.navbarId) } })
    }
    if (tab.value === 'accueil') await ContentService.WriteFile('accueil', html.value, HOME_FILE)
    message.value = 'Enregistré.'
    if (tab.value !== 'accueil') { await load(); form.value = null }
    refreshSite()
  } catch (e: any) { error.value = e.message }
}

async function del(row: any) {
  if (!confirm(`Supprimer « ${row.nom ?? row.file} » ?`)) return
  try {
    if (tab.value === 'equipe') await EquipeService.Delete(row.id)
    if (tab.value === 'navbar') await NavbarService.Delete(row.id)
    if (tab.value === 'content') await ContentService.Delete(row.id)
    if (tab.value === 'charte') await CharteGraphiqueService.Delete(row.id)
    await load(); refreshSite()
  } catch (e: any) { error.value = e.message }
}

async function upload(field: string, folder: 'images' | 'icon' | 'navbar', e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const path = await uploadFile(file, folder)
  form.value[field] = path
  if (field === 'fichier') html.value = await ContentService.ReadFile(path).catch(() => '')
}
</script>

<template>
  <main id="contenu" class="admin">
    <header class="banner"><span class="crumb">Administration</span><h1>Contenu du site</h1><div class="banner__cut" /></header>

    <div class="tabs" role="tablist" style="margin:24px 0">
      <button v-for="t in TABS" :key="t.key" role="tab" type="button" :aria-selected="tab === t.key" @click="tab = t.key">{{ t.label }}</button>
    </div>
    <p v-if="error" class="admin__error" role="alert">{{ error }}</p>
    <p v-if="message" class="muted">{{ message }}</p>

    <!-- ═══ Listes ═══ -->
    <div v-if="tab !== 'accueil'" class="table-wrap">
      <table>
        <thead v-if="tab === 'content'"><tr><th>Ordre</th><th>Nom</th><th>Route</th><th>Navbar</th><th>Fichier</th><th /></tr></thead>
        <thead v-else-if="tab === 'navbar'"><tr><th>Ordre</th><th>Nom</th><th /></tr></thead>
        <thead v-else-if="tab === 'equipe'"><tr><th>Photo</th><th>Nom</th><th>Prénom</th><th>Poste</th><th>Email</th><th>GitHub</th><th /></tr></thead>
        <thead v-else><tr><th>Version</th><th>Date</th><th>Fichier</th><th /></tr></thead>
        <tbody>
          <template v-if="tab === 'content'">
            <tr v-for="c in contents" :key="c.id">
              <td>{{ c.ordre }}</td><td><b>{{ c.nom }}</b></td><td><NuxtLink :to="c.route">{{ c.route }}</NuxtLink></td>
              <td>{{ (c.navbaridTNavbar as Navbar)?.nom }}</td><td class="admin__cell">{{ c.fichier || '—' }}</td>
              <td class="admin__actions"><button class="btn btn-secondary" type="button" @click="edit(c)">Modifier</button><button class="btn" type="button" @click="del(c)">Supprimer</button></td>
            </tr>
          </template>
          <template v-else-if="tab === 'navbar'">
            <tr v-for="n in navbars" :key="n.id">
              <td>{{ n.ordre }}</td><td><b>{{ n.nom }}</b></td>
              <td class="admin__actions"><button class="btn btn-secondary" type="button" @click="edit(n)">Modifier</button><button class="btn" type="button" @click="del(n)">Supprimer</button></td>
            </tr>
          </template>
          <template v-else-if="tab === 'equipe'">
            <tr v-for="m in equipe" :key="m.id">
              <td><img v-if="m.photo" :src="m.photo" alt="" style="height:32px;border-radius:6px"></td>
              <td><b>{{ m.nom }}</b></td><td>{{ m.prenom }}</td><td>{{ m.poste }}</td><td>{{ m.email }}</td>
              <td class="admin__cell">{{ m.github }}</td>
              <td class="admin__actions"><button class="btn btn-secondary" type="button" @click="edit(m)">Modifier</button><button class="btn" type="button" @click="del(m)">Supprimer</button></td>
            </tr>
          </template>
          <template v-else>
            <tr v-for="c in chartes" :key="c.id">
              <td>{{ c.version }}</td><td>{{ c.dateAjout }}</td><td><a :href="c.file" target="_blank">{{ c.file }}</a></td>
              <td class="admin__actions"><button class="btn btn-secondary" type="button" @click="edit(c)">Modifier</button><button class="btn" type="button" @click="del(c)">Supprimer</button></td>
            </tr>
          </template>
        </tbody>
      </table>
      <button class="btn btn-primary" type="button" style="margin-top:12px" @click="edit()">Ajouter</button>
    </div>

    <!-- ═══ Formulaires ═══ -->
    <form v-if="form" class="card admin__form" @submit.prevent="save">
      <template v-if="tab === 'equipe'">
        <h3>{{ form.id ? `Modifier #${form.id}` : 'Ajouter un membre' }}</h3>
        <div class="admin__fields">
          <label class="field"><span>Nom *</span><input v-model="form.nom" required></label>
          <label class="field"><span>Prénom</span><input v-model="form.prenom"></label>
          <label class="field"><span>Poste</span><input v-model="form.poste"></label>
          <label class="field"><span>Email</span><input v-model="form.email" type="email"></label>
          <label class="field"><span>GitHub</span><input v-model="form.github" type="url"></label>
          <label class="field"><span>LinkedIn</span><input v-model="form.linkedin" type="url"></label>
          <label class="field"><span>Photo (chemin)</span><input v-model="form.photo" placeholder="/upload/images/..."><input type="file" accept="image/*" @change="upload('photo', 'images', $event)"></label>
        </div>
      </template>

      <template v-else-if="tab === 'navbar'">
        <h3>{{ form.id ? `Modifier #${form.id}` : 'Ajouter une navbar' }}</h3>
        <div class="admin__fields">
          <label class="field"><span>Nom *</span><input v-model="form.nom" required></label>
          <label class="field"><span>Ordre *</span><input v-model="form.ordre" type="number" required></label>
        </div>
      </template>

      <template v-else-if="tab === 'charte'">
        <h3>{{ form.id ? `Modifier #${form.id}` : 'Ajouter une charte' }}</h3>
        <div class="admin__fields">
          <label class="field"><span>Fichier (chemin) *</span><input v-model="form.file" required placeholder="/upload/icon/..."><input type="file" accept=".pdf,image/*" @change="upload('file', 'icon', $event)"></label>
          <label class="field"><span>Version</span><input v-model="form.version"></label>
          <label class="field"><span>Date d’ajout</span><input v-model="form.dateAjout" type="date"></label>
        </div>
      </template>

      <template v-else>
        <template v-if="tab === 'content'">
          <h3>{{ form.id ? `Modifier « ${form.nom} »` : 'Ajouter une page' }}</h3>
          <div class="admin__fields admin__fields--4">
            <label class="field"><span>Nom *</span><input v-model="form.nom" required></label>
            <label class="field"><span>Route *</span><input v-model="form.route" required placeholder="/docs/guide"></label>
            <label class="field"><span>Navbar *</span>
              <select v-model="form.navbarId" required><option v-for="n in navbars" :key="n.id" :value="n.id">{{ n.nom }}</option></select>
            </label>
            <label class="field"><span>Ordre *</span><input v-model="form.ordre" type="number" required></label>
          </div>
          <label class="field"><span>Fichier (chemin enregistré dans t_content.fichier)</span>
            <div style="display:flex;gap:8px"><input v-model="form.fichier" style="flex:1" placeholder="créé à l’enregistrement : /upload/navbar/..."><input type="file" accept=".html,.htm" @change="upload('fichier', 'navbar', $event)"></div>
          </label>
        </template>
        <h3 v-else>Page d’accueil <small class="muted">({{ HOME_FILE }})</small></h3>

        <template v-if="form.route !== '/playground'">
          <div class="page-editor__bar">
            <div class="tabs" role="tablist" style="margin:0">
              <button type="button" role="tab" :aria-selected="editorMode === 'visual'" @click="editorMode = 'visual'">Éditeur visuel</button>
              <button type="button" role="tab" :aria-selected="editorMode === 'code'" @click="editorMode = 'code'">Code HTML</button>
            </div>
            <p v-if="editorMode === 'code'" class="muted" style="font-size:.8rem;margin:0">
              Composants : <code>&lt;!--component:TeamGrid/--&gt;</code>, <code>&lt;!--component:CompatibilityTable--&gt;…&lt;!--/component--&gt;</code>,
              <code>&lt;!--component:FrameworkCatalog--&gt;…&lt;!--/component--&gt;</code> · charte : <code v-pre>{{charte}}</code> ·
              <code>&lt;h2 id="..."&gt;</code> = sommaire « On this page ».
            </p>
            <p v-else class="muted" style="font-size:.8rem;margin:0">Titres, paragraphes, listes, tableaux, boutons, images… sans écrire de code. Le HTML est généré tout seul.</p>
          </div>
          <div class="page-editor__panes">
            <ClientOnly v-if="editorMode === 'visual'">
              <PageBlockEditor v-model="html" :routes="routes" class="page-editor__visual" />
            </ClientOnly>
            <textarea v-else v-model="html" class="page-editor__code" spellcheck="false" />
            <div class="page-editor__preview content">
              <header v-if="tab === 'content'" class="banner"><span class="crumb">{{ navbars.find(n => n.id === Number(form.navbarId))?.nom }} / {{ form.nom }}</span><h1>{{ form.nom }}</h1><div class="banner__cut" /></header>
              <PageRenderer :html="html" />
            </div>
          </div>
        </template>
        <p v-else class="muted">/playground est une page applicative : pas de fichier de contenu.</p>
      </template>

      <div style="display:flex;gap:8px;align-items:center;margin-top:12px">
        <button class="btn btn-primary" type="submit">Enregistrer</button>
        <button v-if="tab !== 'accueil'" class="btn btn-secondary" type="button" @click="form = null">Fermer</button>
        <NuxtLink v-if="tab === 'accueil' || form.id" class="btn" :to="tab === 'accueil' ? '/' : form.route" target="_blank">Voir la page</NuxtLink>
      </div>
    </form>
  </main>
</template>

<style scoped>
.admin { max-width: 1360px; margin: 0 auto; padding: 32px 24px 96px; }
.admin__error { color: #ff6b6b; background: rgba(255, 80, 80, .1); border: 1px solid rgba(255, 80, 80, .5); padding: 10px 12px; border-radius: 8px; }
.admin__cell { max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.admin__actions { white-space: nowrap; text-align: right; }
.admin__actions .btn { margin-left: 6px; }
.admin__form { margin-top: 24px; padding: 20px; display: flex; flex-direction: column; gap: 12px; }
.admin__fields { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.admin__fields--4 { grid-template-columns: repeat(4, 1fr); }
.field { display: flex; flex-direction: column; gap: 4px; font-size: .85rem; }
.field input, .field select { padding: 8px 10px; border-radius: 6px; border: 1px solid var(--line); background: var(--bg); color: var(--text); font: inherit; }
.page-editor__bar { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.page-editor__visual { min-height: 0; }
.page-editor__panes { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; height: 70vh; }
.page-editor__code { font-family: var(--font-mono); font-size: .8rem; line-height: 1.5; padding: 12px; border-radius: 8px;
  border: 1px solid var(--line); background: var(--code-bg); color: var(--code-text); resize: none; height: 100%; }
.page-editor__preview { overflow-y: auto; height: 100%; padding: 0 16px 32px; border-radius: 8px; background: var(--bg-dark); border: 1px solid var(--line); }
@media (max-width: 900px) { .admin__fields, .admin__fields--4, .page-editor__panes { grid-template-columns: 1fr; } }
</style>