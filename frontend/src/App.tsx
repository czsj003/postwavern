import { Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AuthPage } from './pages/AuthPage'
import { HomePage } from './pages/HomePage'
import { NotFoundPage } from './pages/NotFoundPage'
import { PostDetailPage } from './pages/PostDetailPage'
import { PostEditorPage } from './pages/PostEditorPage'

export default function App() {
  return <Routes>
    <Route path="/login" element={<AuthPage mode="login" />} />
    <Route path="/signup" element={<AuthPage mode="signup" />} />
    <Route element={<Layout />}>
      <Route index element={<HomePage />} />
      <Route path="posts/:id" element={<PostDetailPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="write" element={<PostEditorPage />} />
        <Route path="posts/:id/edit" element={<PostEditorPage />} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Route>
  </Routes>
}
