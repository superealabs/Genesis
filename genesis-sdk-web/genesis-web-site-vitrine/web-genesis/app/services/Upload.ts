import { siteApi } from './http'
export async function uploadFile(file: File, folder: 'images' | 'icon' | 'navbar' = 'images') {
  const fd = new FormData()
  fd.append('file', file)
  return (await siteApi<{ path: string }>('/upload', { method: 'POST', body: fd, query: { folder } })).path
}
