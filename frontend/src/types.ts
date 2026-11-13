export interface User {
  id: number
  name: string
  email: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface Post {
  id: number
  title: string
  content: string
  imageName: string | null
  userID: number
  authorName: string
  listOfUsersWaved: number[]
  waveCount: number
  wavedByCurrentUser: boolean
  createdAt: string
  updatedAt: string
}

export interface Waver {
  id: number
  name: string
  email: string
}

export interface WaveResult {
  message: string
  count: number
  waved: boolean
}

export interface ApiErrorBody {
  code?: string
  message?: string
  fieldErrors?: Record<string, string>
}
