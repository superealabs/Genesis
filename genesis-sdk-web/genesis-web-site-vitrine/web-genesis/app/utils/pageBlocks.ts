/**
 * Éditeur de page sans code : conversion blocs <-> HTML.
 *
 * Le HTML produit utilise les classes du site (site.css) et les marqueurs de PageRenderer :
 *   <!--component:TeamGrid/-->  ·  <!--component:X--> … <!--/component-->  ·  {{charte}}
 *
 * Texte riche minimal dans les paragraphes, listes et cellules :
 *   **gras**   *italique*   `code`   [texte du lien](/route ou https://...)
 * Tout ce que l'éditeur ne sait pas représenter est gardé tel quel dans un bloc « HTML ».
 */

export type ButtonStyle = 'primary' | 'accent' | 'secondary'
export interface PageButton { label: string; href: string; style: ButtonStyle; newTab: boolean }

export type Block =
  | { id: number; type: 'heading'; level: 1 | 2 | 3; text: string; anchor: string }
  | { id: number; type: 'paragraph'; text: string; lead: boolean }
  | { id: number; type: 'list'; ordered: boolean; items: string[] }
  | { id: number; type: 'table'; header: boolean; rows: string[][] }
  | { id: number; type: 'buttons'; buttons: PageButton[] }
  | { id: number; type: 'image'; src: string; alt: string; caption: string }
  | { id: number; type: 'code'; code: string }
  | { id: number; type: 'component'; name: string; inner: string }
  | { id: number; type: 'html'; html: string }

export type BlockType = Block['type']

let seq = 0
export const nextId = () => ++seq

export const COMPONENT_NAMES = ['TeamGrid', 'CompatibilityTable', 'FrameworkCatalog'] as const

export function newBlock(type: BlockType): Block {
  const id = nextId()
  switch (type) {
    case 'heading': return { id, type, level: 2, text: 'Nouveau titre', anchor: '' }
    case 'paragraph': return { id, type, text: '', lead: false }
    case 'list': return { id, type, ordered: false, items: ['Premier point', 'Deuxième point'] }
    case 'table': return { id, type, header: true, rows: [['Colonne 1', 'Colonne 2'], ['', '']] }
    case 'buttons': return { id, type, buttons: [{ label: 'Commencer', href: '/', style: 'primary', newTab: false }] }
    case 'image': return { id, type, src: '', alt: '', caption: '' }
    case 'code': return { id, type, code: '' }
    case 'component': return { id, type, name: 'TeamGrid', inner: '' }
    case 'html': return { id, type, html: '<div class="card">\n  <p>Votre HTML</p>\n</div>' }
  }
}

// ─── Texte riche minimal ────────────────────────────────────────────────

const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
const escAttr = (s: string) => esc(s).replace(/"/g, '&quot;')

/** Lien sûr : chemins du site, ancres, http(s), mailto, {{charte}} */
export function safeHref(href: string) {
  const h = href.trim()
  return /^(\/|#|https?:\/\/|mailto:|\{\{charte\}\})/i.test(h) ? h : `/${h.replace(/^\.?\/*/, '')}`
}

export function inlineToHtml(text: string) {
  let s = esc(text)
  s = s.replace(/`([^`]+)`/g, '<code>$1</code>')
  s = s.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (_m, label, href) => {
    const h = safeHref(href.replace(/&amp;/g, '&'))
    const ext = /^https?:\/\//i.test(h)
    return `<a href="${escAttr(h)}"${ext ? ' target="_blank" rel="noopener"' : ''}>${label}</a>`
  })
  s = s.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  s = s.replace(/(^|[^*])\*([^*\s][^*]*)\*/g, '$1<em>$2</em>')
  return s.replace(/\n/g, '<br>')
}

/** Inverse de inlineToHtml, ou null si l'élément contient autre chose (gardé alors en HTML brut) */
function htmlToInline(el: Element): string | null {
  let out = ''
  for (const n of Array.from(el.childNodes)) {
    if (n.nodeType === Node.TEXT_NODE) { out += (n.textContent || '').replace(/\s+/g, ' '); continue }
    if (n.nodeType !== Node.ELEMENT_NODE) continue
    const c = n as Element
    const tag = c.tagName.toLowerCase()
    const inner = htmlToInline(c)
    if (inner === null) return null
    if (tag === 'strong' || tag === 'b') out += `**${inner}**`
    else if (tag === 'em' || tag === 'i') out += `*${inner}*`
    else if (tag === 'code') out += `\`${c.textContent}\``
    else if (tag === 'br') out += '\n'
    else if (tag === 'a' && !c.className) out += `[${inner}](${c.getAttribute('href') || ''})`
    else return null
  }
  return out
}

export function slugify(text: string) {
  return text.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 60)
}

// ─── Blocs -> HTML ──────────────────────────────────────────────────────

function blockToHtml(b: Block): string {
  switch (b.type) {
    case 'heading': {
      const id = b.level === 1 ? '' : (b.anchor.trim() || slugify(b.text))
      return `<h${b.level}${id ? ` id="${escAttr(id)}"` : ''}>${inlineToHtml(b.text)}</h${b.level}>`
    }
    case 'paragraph':
      return `<p${b.lead ? ' class="lead"' : ''}>${inlineToHtml(b.text)}</p>`
    case 'list': {
      const tag = b.ordered ? 'ol' : 'ul'
      return `<${tag}>\n${b.items.filter(i => i.trim()).map(i => `  <li>${inlineToHtml(i)}</li>`).join('\n')}\n</${tag}>`
    }
    case 'table': {
      const [head, ...body] = b.header ? b.rows : [undefined, ...b.rows]
      const row = (cells: string[], tag: 'th' | 'td') => `<tr>${cells.map(c => `<${tag}>${inlineToHtml(c)}</${tag}>`).join('')}</tr>`
      return `<div class="table-wrap"><table>\n${head ? `  <thead>${row(head, 'th')}</thead>\n` : ''}  <tbody>\n${body.map(r => `    ${row(r, 'td')}`).join('\n')}\n  </tbody>\n</table></div>`
    }
    case 'buttons':
      return `<p class="btn-row">${b.buttons.filter(x => x.label.trim()).map(x =>
        `<a class="btn btn-${x.style}" href="${escAttr(safeHref(x.href || '/'))}"${x.newTab ? ' target="_blank" rel="noopener"' : ''}>${esc(x.label)}</a>`
      ).join(' ')}</p>`
    case 'image':
      if (!b.src) return ''
      return b.caption
        ? `<figure class="page-figure"><img src="${escAttr(b.src)}" alt="${escAttr(b.alt)}"><figcaption>${inlineToHtml(b.caption)}</figcaption></figure>`
        : `<img class="page-img" src="${escAttr(b.src)}" alt="${escAttr(b.alt)}">`
    case 'code':
      return `<pre><code>${esc(b.code)}</code></pre>`
    case 'component':
      return b.inner.trim() ? `<!--component:${b.name}-->${b.inner}<!--/component-->` : `<!--component:${b.name}/-->`
    case 'html':
      return b.html
  }
}

export function blocksToHtml(blocks: Block[]) {
  return blocks.map(blockToHtml).filter(Boolean).join('\n\n') + '\n'
}

// ─── HTML -> blocs ──────────────────────────────────────────────────────

const rawBlock = (html: string): Block => ({ id: nextId(), type: 'html', html: html.trim() })

function elementToBlock(el: Element): Block {
  const tag = el.tagName.toLowerCase()
  const raw = () => rawBlock(el.outerHTML)

  if (/^h[1-3]$/.test(tag)) {
    const text = htmlToInline(el)
    if (text === null || el.attributes.length > (el.id ? 1 : 0)) return raw()
    const level = Number(tag[1]) as 1 | 2 | 3
    const id = el.id && el.id !== slugify(text) ? el.id : ''
    return { id: nextId(), type: 'heading', level, text: text.trim(), anchor: id }
  }

  if (tag === 'p') {
    const links = Array.from(el.children)
    if (el.classList.contains('btn-row') || (links.length && links.every(a => a.matches('a.btn')) && !el.textContent!.replace(links.map(a => a.textContent).join(''), '').trim())) {
      return {
        id: nextId(), type: 'buttons',
        buttons: links.map(a => ({
          label: a.textContent!.trim(),
          href: a.getAttribute('href') || '/',
          style: (['primary', 'accent', 'secondary'].find(s => a.classList.contains(`btn-${s}`)) || 'secondary') as ButtonStyle,
          newTab: a.getAttribute('target') === '_blank'
        }))
      }
    }
    const cls = el.getAttribute('class')
    const text = htmlToInline(el)
    if (text === null || (cls && cls !== 'lead') || el.attributes.length > (cls ? 1 : 0)) return raw()
    return { id: nextId(), type: 'paragraph', text: text.trim(), lead: cls === 'lead' }
  }

  if ((tag === 'ul' || tag === 'ol') && !el.attributes.length) {
    const items = Array.from(el.children).map(li => (li.tagName === 'LI' && !li.attributes.length ? htmlToInline(li) : null))
    if (items.some(i => i === null)) return raw()
    return { id: nextId(), type: 'list', ordered: tag === 'ol', items: (items as string[]).map(i => i.trim()) }
  }

  const table = tag === 'table' ? el : (tag === 'div' && el.className === 'table-wrap' && el.children.length === 1 && el.firstElementChild!.tagName === 'TABLE' ? el.firstElementChild! : null)
  if (table && !table.attributes.length) {
    const head = table.querySelector(':scope > thead > tr')
    const bodyRows = Array.from(table.querySelectorAll(':scope > tbody > tr, :scope > tr'))
    const toCells = (tr: Element) => Array.from(tr.children).map(c => (c.attributes.length ? null : htmlToInline(c)))
    const rows = [...(head ? [toCells(head)] : []), ...bodyRows.map(toCells)]
    if (rows.some(r => r.some(c => c === null))) return raw()
    const width = Math.max(1, ...rows.map(r => r.length))
    return { id: nextId(), type: 'table', header: !!head, rows: rows.map(r => Array.from({ length: width }, (_, i) => (r[i] as string | undefined)?.trim() ?? '')) }
  }

  if (tag === 'img' && el.getAttribute('src')) {
    return { id: nextId(), type: 'image', src: el.getAttribute('src')!, alt: el.getAttribute('alt') || '', caption: '' }
  }
  if (tag === 'figure' && el.querySelector('img') && el.children.length <= 2) {
    const img = el.querySelector('img')!
    const cap = el.querySelector('figcaption')
    const caption = cap ? htmlToInline(cap) : ''
    if (caption === null) return raw()
    return { id: nextId(), type: 'image', src: img.getAttribute('src') || '', alt: img.getAttribute('alt') || '', caption }
  }

  if (tag === 'pre' && !el.attributes.length && (el.children.length === 0 || (el.children.length === 1 && el.firstElementChild!.tagName === 'CODE' && !el.firstElementChild!.children.length))) {
    return { id: nextId(), type: 'code', code: el.textContent || '' }
  }

  return raw()
}

export function htmlToBlocks(html: string): Block[] {
  if (!html.trim()) return []
  const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html')
  const nodes = Array.from(doc.body.childNodes)
  const blocks: Block[] = []
  for (let i = 0; i < nodes.length; i++) {
    const n = nodes[i]!
    if (n.nodeType === Node.COMMENT_NODE) {
      const c = (n.textContent || '').trim()
      const self = c.match(/^component:(\w+)\/$/)
      if (self) { blocks.push({ id: nextId(), type: 'component', name: self[1]!, inner: '' }); continue }
      const open = c.match(/^component:(\w+)$/)
      if (open) {
        let inner = ''
        let j = i + 1
        for (; j < nodes.length; j++) {
          const m = nodes[j]!
          if (m.nodeType === Node.COMMENT_NODE && (m.textContent || '').trim() === '/component') break
          inner += m.nodeType === Node.ELEMENT_NODE ? (m as Element).outerHTML : m.nodeType === Node.TEXT_NODE ? m.textContent : `<!--${m.textContent}-->`
        }
        blocks.push({ id: nextId(), type: 'component', name: open[1]!, inner })
        i = j
        continue
      }
      blocks.push(rawBlock(`<!--${n.textContent}-->`))
      continue
    }
    if (n.nodeType === Node.TEXT_NODE) {
      const t = (n.textContent || '').trim()
      if (t) blocks.push({ id: nextId(), type: 'paragraph', text: t, lead: false })
      continue
    }
    if (n.nodeType === Node.ELEMENT_NODE) blocks.push(elementToBlock(n as Element))
  }
  return blocks
}