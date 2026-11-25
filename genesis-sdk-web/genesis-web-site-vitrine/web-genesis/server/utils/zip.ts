// Compresse un dossier en .zip (en mémoire) avec fflate
import { readdirSync, readFileSync, statSync } from 'node:fs'
import { join, relative, sep } from 'node:path'
import { zipSync, type Zippable } from 'fflate'

export function zipFolder(root: string): Uint8Array {
  const files: Zippable = {}
  const walk = (dir: string) => {
    for (const name of readdirSync(dir)) {
      const full = join(dir, name)
      if (statSync(full).isDirectory()) walk(full)
      else files[relative(root, full).split(sep).join('/')] = readFileSync(full)
    }
  }
  walk(root)
  return zipSync(files, { level: 6 })
}
