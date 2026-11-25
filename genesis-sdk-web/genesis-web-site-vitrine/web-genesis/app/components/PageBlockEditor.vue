<script setup lang="ts">
// Éditeur de page sans code : la page est une liste de blocs (titre, paragraphe, liste, tableau,
// boutons, image, code, composant, HTML). Chaque modification réécrit le HTML (v-model).
import {
  blocksToHtml, htmlToBlocks, newBlock, nextId, slugify, COMPONENT_NAMES,
  type Block, type BlockType, type ButtonStyle
} from '~/utils/pageBlocks'
import { uploadFile } from '~/services/Upload'

const html = defineModel<string>({ required: true })
const props = defineProps<{ routes?: string[] }>()

const blocks = ref<Block[]>([])
let lastEmitted: string | null = null
let parsing = false   // blocs venant du HTML : ne pas réécrire le HTML tant que rien n'est modifié

// HTML -> blocs (au chargement d'une page, ou si le HTML a été modifié dans l'onglet « Code HTML »)
watch(html, (v) => {
  if (import.meta.server || v === lastEmitted) return
  parsing = true
  blocks.value = htmlToBlocks(v || '')
}, { immediate: true })

// blocs -> HTML
watch(blocks, (b) => {
  if (parsing) { parsing = false; return }
  lastEmitted = blocksToHtml(b)
  html.value = lastEmitted
}, { deep: true })

const ADD: { type: BlockType; label: string; hint: string }[] = [
  { type: 'heading', label: 'Titre', hint: 'H1, H2 ou H3' },
  { type: 'paragraph', label: 'Paragraphe', hint: 'texte, gras, liens' },
  { type: 'list', label: 'Liste', hint: 'à puces ou numérotée' },
  { type: 'table', label: 'Tableau', hint: 'lignes et colonnes' },
  { type: 'buttons', label: 'Boutons', hint: 'liens en forme de bouton' },
  { type: 'image', label: 'Image', hint: 'envoi ou chemin' },
  { type: 'code', label: 'Code', hint: 'bloc de code' },
  { type: 'component', label: 'Composant', hint: 'équipe, frameworks…' },
  { type: 'html', label: 'HTML', hint: 'votre propre code' }
]
const LABEL = Object.fromEntries(ADD.map(a => [a.type, a.label])) as Record<BlockType, string>
const STYLES: { value: ButtonStyle; label: string }[] = [
  { value: 'primary', label: 'Vert (principal)' }, { value: 'accent', label: 'Jaune' }, { value: 'secondary', label: 'Bleu' }
]
const COMPONENT_LABEL: Record<string, string> = {
  TeamGrid: 'Équipe (t_equipe)', CompatibilityTable: 'Tableau de compatibilité', FrameworkCatalog: 'Catalogue des frameworks'
}

const adding = ref<number | null>(null)   // index où insérer (menu ouvert)
function add(type: BlockType, at: number) {
  blocks.value.splice(at, 0, newBlock(type))
  adding.value = null
}
function move(i: number, d: -1 | 1) {
  const j = i + d
  if (j < 0 || j >= blocks.value.length) return
  const [b] = blocks.value.splice(i, 1)
  blocks.value.splice(j, 0, b!)
}
function duplicate(i: number) {
  blocks.value.splice(i + 1, 0, { ...JSON.parse(JSON.stringify(blocks.value[i])), id: nextId() })
}
function remove(i: number) {
  blocks.value.splice(i, 1)
}

// Tableau
function addRow(b: Extract<Block, { type: 'table' }>) { b.rows.push(Array(b.rows[0]?.length || 1).fill('')) }
function addCol(b: Extract<Block, { type: 'table' }>) { b.rows.forEach(r => r.push('')) }
function delRow(b: Extract<Block, { type: 'table' }>, r: number) { if (b.rows.length > 1) b.rows.splice(r, 1) }
function delCol(b: Extract<Block, { type: 'table' }>, c: number) { if ((b.rows[0]?.length || 0) > 1) b.rows.forEach(r => r.splice(c, 1)) }

// Liste : une ligne = un point
const listText = (b: Extract<Block, { type: 'list' }>) => b.items.join('\n')
function setList(b: Extract<Block, { type: 'list' }>, v: string) { b.items = v.split('\n') }

// Mise en forme dans un paragraphe : entoure la sélection
function wrap(e: Event, b: { text: string }, before: string, after = before) {
  const ta = (e.currentTarget as HTMLElement).closest('.pbe-block')!.querySelector('textarea')!
  const { selectionStart: s, selectionEnd: f } = ta
  const sel = b.text.slice(s, f) || 'texte'
  b.text = b.text.slice(0, s) + before + sel + after + b.text.slice(f)
  nextTick(() => { ta.focus(); ta.setSelectionRange(s + before.length, s + before.length + sel.length) })
}
function link(e: Event, b: { text: string }) {
  const url = prompt('Adresse du lien (ex. /docs/guide ou https://…)', '/')
  if (url) wrap(e, b, '[', `](${url})`)
}

const uploading = ref<number | null>(null)
async function uploadImage(b: Extract<Block, { type: 'image' }>, e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = b.id
  try { b.src = await uploadFile(file, 'images'); if (!b.alt) b.alt = file.name.replace(/\.[^.]+$/, '') }
  catch (err: any) { alert(err?.message || 'Envoi impossible') }
  finally { uploading.value = null }
}
</script>

<template>
  <div class="pbe">
    <datalist id="pbe-routes"><option v-for="r in props.routes" :key="r" :value="r" /></datalist>

    <p v-if="!blocks.length" class="pbe-empty">Page vide : ajoutez un premier bloc.</p>

    <template v-for="(b, i) in blocks" :key="b.id">
      <!-- Insérer avant ce bloc -->
      <div class="pbe-insert">
        <button type="button" class="pbe-insert__btn" :aria-expanded="adding === i" @click="adding = adding === i ? null : i">+ Ajouter ici</button>
        <div v-if="adding === i" class="pbe-menu">
          <button v-for="a in ADD" :key="a.type" type="button" @click="add(a.type, i)"><b>{{ a.label }}</b><small>{{ a.hint }}</small></button>
        </div>
      </div>

      <section class="pbe-block">
        <header class="pbe-block__head">
          <span class="pbe-block__type">{{ LABEL[b.type] }}</span>
          <select v-if="b.type === 'heading'" v-model.number="b.level" class="pbe-mini" aria-label="Niveau du titre">
            <option :value="1">H1 — titre de page</option><option :value="2">H2 — section</option><option :value="3">H3 — sous-section</option>
          </select>
          <span class="pbe-block__tools">
            <button type="button" title="Monter" :disabled="i === 0" @click="move(i, -1)">↑</button>
            <button type="button" title="Descendre" :disabled="i === blocks.length - 1" @click="move(i, 1)">↓</button>
            <button type="button" title="Dupliquer" @click="duplicate(i)">⧉</button>
            <button type="button" title="Supprimer" class="pbe-del" @click="remove(i)">✕</button>
          </span>
        </header>

        <!-- Titre -->
        <template v-if="b.type === 'heading'">
          <input v-model="b.text" class="pbe-input" :class="`pbe-h${b.level}`" placeholder="Texte du titre">
          <label v-if="b.level > 1" class="pbe-sub">Ancre (sommaire « On this page ») :
            <input v-model="b.anchor" class="pbe-mini" :placeholder="slugify(b.text) || 'auto'">
          </label>
        </template>

        <!-- Paragraphe -->
        <template v-else-if="b.type === 'paragraph'">
          <div class="pbe-format">
            <button type="button" title="Gras" @click="wrap($event, b, '**')"><b>G</b></button>
            <button type="button" title="Italique" @click="wrap($event, b, '*')"><i>I</i></button>
            <button type="button" title="Code" @click="wrap($event, b, '`')">&lt;/&gt;</button>
            <button type="button" title="Lien" @click="link($event, b)">Lien</button>
            <label class="pbe-sub"><input v-model="b.lead" type="checkbox"> Texte d'introduction (plus grand)</label>
          </div>
          <textarea v-model="b.text" class="pbe-input" rows="3" placeholder="Votre texte… **gras**, *italique*, [lien](/route)" />
        </template>

        <!-- Liste -->
        <template v-else-if="b.type === 'list'">
          <label class="pbe-sub"><input v-model="b.ordered" type="checkbox"> Numérotée (1, 2, 3…)</label>
          <textarea :value="listText(b)" class="pbe-input" rows="4" placeholder="Un point par ligne" @input="setList(b, ($event.target as HTMLTextAreaElement).value)" />
          <small class="pbe-help">Un point par ligne. **gras**, *italique* et [lien](/route) fonctionnent aussi.</small>
        </template>

        <!-- Tableau -->
        <template v-else-if="b.type === 'table'">
          <label class="pbe-sub"><input v-model="b.header" type="checkbox"> Première ligne = en-tête</label>
          <div class="pbe-table">
            <table>
              <tr>
                <td v-for="(_, c) in b.rows[0]" :key="'x' + c" class="pbe-table__ctl">
                  <button type="button" title="Supprimer la colonne" @click="delCol(b, c)">✕</button>
                </td>
                <td />
              </tr>
              <tr v-for="(row, r) in b.rows" :key="r" :class="{ 'is-head': b.header && r === 0 }">
                <td v-for="(_, c) in row" :key="c"><input v-model="row[c]" :placeholder="b.header && r === 0 ? 'En-tête' : ''"></td>
                <td class="pbe-table__ctl"><button type="button" title="Supprimer la ligne" @click="delRow(b, r)">✕</button></td>
              </tr>
            </table>
          </div>
          <div class="pbe-row">
            <button type="button" class="btn btn-secondary btn-sm" @click="addRow(b)">+ Ligne</button>
            <button type="button" class="btn btn-secondary btn-sm" @click="addCol(b)">+ Colonne</button>
          </div>
        </template>

        <!-- Boutons -->
        <template v-else-if="b.type === 'buttons'">
          <div v-for="(x, k) in b.buttons" :key="k" class="pbe-button">
            <input v-model="x.label" class="pbe-input" placeholder="Texte du bouton">
            <input v-model="x.href" class="pbe-input" list="pbe-routes" placeholder="/docs/guide ou https://…">
            <select v-model="x.style" class="pbe-mini"><option v-for="s in STYLES" :key="s.value" :value="s.value">{{ s.label }}</option></select>
            <label class="pbe-sub"><input v-model="x.newTab" type="checkbox"> Nouvel onglet</label>
            <button type="button" class="pbe-del" title="Retirer ce bouton" @click="b.buttons.splice(k, 1)">✕</button>
          </div>
          <div class="pbe-row">
            <button type="button" class="btn btn-secondary btn-sm" @click="b.buttons.push({ label: 'Nouveau bouton', href: '/', style: 'secondary', newTab: false })">+ Bouton</button>
            <span class="pbe-preview-btns"><a v-for="(x, k) in b.buttons" :key="k" :class="`btn btn-${x.style} btn-sm`">{{ x.label || '…' }}</a></span>
          </div>
        </template>

        <!-- Image -->
        <template v-else-if="b.type === 'image'">
          <div class="pbe-button">
            <input v-model="b.src" class="pbe-input" placeholder="/upload/images/…">
            <label class="btn btn-secondary btn-sm">{{ uploading === b.id ? 'Envoi…' : 'Envoyer une image' }}
              <input type="file" accept="image/*" hidden @change="uploadImage(b, $event)">
            </label>
          </div>
          <input v-model="b.alt" class="pbe-input" placeholder="Description de l'image (accessibilité)">
          <input v-model="b.caption" class="pbe-input" placeholder="Légende (facultatif)">
          <img v-if="b.src" :src="b.src" alt="" class="pbe-thumb">
        </template>

        <!-- Code -->
        <textarea v-else-if="b.type === 'code'" v-model="b.code" class="pbe-input pbe-mono" rows="5" spellcheck="false" placeholder="Code affiché tel quel" />

        <!-- Composant -->
        <template v-else-if="b.type === 'component'">
          <select v-model="b.name" class="pbe-mini">
            <option v-for="n in COMPONENT_NAMES" :key="n" :value="n">{{ COMPONENT_LABEL[n] }}</option>
          </select>
          <small class="pbe-help">Le contenu est lu dans la base au moment de l'affichage.</small>
          <details v-if="b.name !== 'TeamGrid'" class="pbe-details">
            <summary>HTML de secours (si l'API est arrêtée)</summary>
            <textarea v-model="b.inner" class="pbe-input pbe-mono" rows="4" spellcheck="false" />
          </details>
        </template>

        <!-- HTML libre -->
        <template v-else>
          <textarea v-model="b.html" class="pbe-input pbe-mono" rows="6" spellcheck="false" />
          <small class="pbe-help">Votre propre code HTML. Classes du site : card, grid grid-2, btn btn-primary, table-wrap, badge…</small>
        </template>
      </section>
    </template>

    <!-- Ajouter à la fin -->
    <div class="pbe-insert pbe-insert--end">
      <button type="button" class="pbe-insert__btn" :aria-expanded="adding === blocks.length" @click="adding = adding === blocks.length ? null : blocks.length">+ Ajouter un bloc</button>
      <div v-if="adding === blocks.length" class="pbe-menu">
        <button v-for="a in ADD" :key="a.type" type="button" @click="add(a.type, blocks.length)"><b>{{ a.label }}</b><small>{{ a.hint }}</small></button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pbe { height: 100%; overflow-y: auto; padding: 4px 6px 24px 2px; display: flex; flex-direction: column; gap: 0; }
.pbe-empty { color: var(--text-soft); font-size: .85rem; margin: 8px 0; }
.pbe-block { background: var(--bg-light); border: 1px solid var(--line); border-radius: var(--r); padding: 10px 12px 12px; display: flex; flex-direction: column; gap: 8px; }
.pbe-block__head { display: flex; align-items: center; gap: 8px; }
.pbe-block__type { font-size: .7rem; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; color: var(--primary-ink); }
.pbe-block__tools { margin-left: auto; display: flex; gap: 2px; }
.pbe-block__tools button, .pbe-format button, .pbe-del, .pbe-table__ctl button {
  min-width: 28px; height: 26px; border: 0; border-radius: var(--r-sm); background: var(--bg); color: var(--text); cursor: pointer; font: 600 .8rem var(--font-body);
}
.pbe-block__tools button:disabled { opacity: .35; cursor: default; }
.pbe-del:hover, .pbe-table__ctl button:hover { background: hsl(0, 70%, 45%); color: #fff; }
.pbe-input, .pbe-mini {
  width: 100%; font: .875rem var(--font-body); color: var(--text); background: var(--bg-dark);
  border: 1px solid var(--line); border-radius: var(--r-sm); padding: 7px 10px; outline: 0;
}
.pbe-input:focus, .pbe-mini:focus { border-color: var(--primary); }
textarea.pbe-input { resize: vertical; line-height: 1.5; }
.pbe-mini { width: auto; padding: 4px 8px; font-size: .78rem; }
.pbe-mono { font-family: var(--font-mono); font-size: .78rem; }
.pbe-h1 { font-size: 1.5rem; font-weight: 600; }
.pbe-h2 { font-size: 1.2rem; font-weight: 600; }
.pbe-h3 { font-size: 1rem; font-weight: 600; }
.pbe-sub { display: inline-flex; align-items: center; gap: 6px; font-size: .78rem; color: var(--text-muted); }
.pbe-help { font-size: .72rem; color: var(--text-soft); }
.pbe-format { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.pbe-format .pbe-sub { margin-left: 8px; }
.pbe-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.pbe-button { display: grid; grid-template-columns: 1fr 1.3fr auto auto auto; gap: 6px; align-items: center; }
.pbe-preview-btns { display: inline-flex; gap: 6px; margin-left: auto; pointer-events: none; }
.pbe-table { overflow-x: auto; }
.pbe-table table { border-collapse: collapse; width: 100%; font-size: .82rem; }
.pbe-table td { padding: 2px; border: 0; background: none; }
.pbe-table input { width: 100%; min-width: 90px; font: inherit; color: var(--text); background: var(--bg-dark); border: 1px solid var(--line); border-radius: 3px; padding: 5px 7px; }
.pbe-table tr.is-head input { font-weight: 700; background: var(--bg); }
.pbe-table__ctl { width: 30px; text-align: center; }
.pbe-thumb { max-height: 120px; width: auto; border-radius: var(--r-sm); align-self: flex-start; }
.pbe-details summary { cursor: pointer; font-size: .78rem; color: var(--text-muted); }
.pbe-insert { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 6px 0; }
.pbe-insert__btn { border: 1px dashed var(--line); background: none; color: var(--text-soft); border-radius: 99px; padding: 2px 14px; font: 500 .72rem var(--font-body); cursor: pointer; opacity: .6; }
.pbe-insert:hover .pbe-insert__btn, .pbe-insert__btn[aria-expanded="true"] { opacity: 1; color: var(--primary-ink); border-color: var(--primary); }
.pbe-insert--end .pbe-insert__btn { opacity: 1; padding: 6px 18px; font-size: .8rem; }
.pbe-menu { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 4px; width: 100%;
  padding: 6px; background: var(--bg-dark); border: 1px solid var(--primary); border-radius: var(--r); }
.pbe-menu button { display: flex; flex-direction: column; align-items: flex-start; gap: 1px; text-align: left; padding: 8px 10px; border: 0; border-radius: var(--r-sm); background: var(--bg-light); color: var(--text); cursor: pointer; }
.pbe-menu button:hover { background: var(--bg-hover); }
.pbe-menu b { font-size: .82rem; }
.pbe-menu small { font-size: .68rem; color: var(--text-soft); }
@media (max-width: 700px) { .pbe-button { grid-template-columns: 1fr; } .pbe-menu { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>