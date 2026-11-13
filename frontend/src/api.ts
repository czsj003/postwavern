import type { ApiErrorBody, LoginResult, Post, User, Waver, WaveResult } from './types'

const API_ROOT = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  status: number
  code?: string
  fieldErrors: Record<string, string>

  constructor(status: number, body: ApiErrorBody) {
    super(body.message || 'Something went wrong. Please try again.')
    this.name = 'ApiError'
    this.status = status
    this.code = body.code
    this.fieldErrors = body.fieldErrors ?? {}
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('postwave_token')
  const headers = new Headers(options.headers)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${API_ROOT}${path}`, { ...options, headers })
  if (!response.ok) {
    let body: ApiErrorBody = {}
    try { body = await response.json() as ApiErrorBody } catch { /* empty body */ }
    if (response.status === 401 && token && !path.startsWith('/api/auth/')) {
      localStorage.removeItem('postwave_token')
      localStorage.removeItem('postwave_user')
      window.dispatchEvent(new Event('postwave:unauthorized'))
    }
    throw new ApiError(response.status, body)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  login: (email: string, password: string) => request<LoginResult>('/api/auth/login', {
    method: 'POST', body: JSON.stringify({ email, password }),
  }),
  signup: (name: string, email: string, password: string) => request<User>('/api/auth/signup', {
    method: 'POST', body: JSON.stringify({ name, email, password }),
  }),
  posts: (popular = false) => request<Post[]>(`/api/posts${popular ? '?filter=popular' : ''}`),
  post: (id: number) => request<Post>(`/api/posts/${id}`),
  createPost: (data: FormData) => request<Post>('/api/posts', { method: 'POST', body: data }),
  updatePost: (id: number, data: FormData) => request<Post>(`/api/posts/${id}`, { method: 'PUT', body: data }),
  deletePost: (id: number) => request<void>(`/api/posts/${id}`, { method: 'DELETE' }),
  toggleWave: (id: number) => request<WaveResult>(`/api/posts/${id}/wave`, { method: 'PATCH' }),
  wavers: (id: number) => request<Waver[]>(`/api/posts/${id}/wavers`),
}

export const imageUrl = (imageName: string) => `${API_ROOT}/uploads/${encodeURIComponent(imageName)}`
