import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError, api, imageUrl } from '../api'
import { useAuth } from '../auth'
import { Loading } from '../components/Loading'

export function PostEditorPage() {
  const { id } = useParams()
  const postId = id ? Number(id) : null
  const editing = postId !== null
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [existingImage, setExistingImage] = useState<string | null>(null)
  const [preview, setPreview] = useState<string | null>(null)
  const [loading, setLoading] = useState(editing)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const navigate = useNavigate()
  const { user } = useAuth()

  useEffect(() => {
    if (!postId) return
    api.post(postId).then((post) => {
      if (post.userID !== user?.id) { navigate(`/posts/${post.id}`, { replace: true }); return }
      setTitle(post.title); setContent(post.content); setExistingImage(post.imageName)
    }).catch((err) => setError(err instanceof Error ? err.message : 'Unable to load story.')).finally(() => setLoading(false))
  }, [postId, user?.id, navigate])

  useEffect(() => {
    if (!file) { setPreview(null); return }
    const url = URL.createObjectURL(file)
    setPreview(url)
    return () => URL.revokeObjectURL(url)
  }, [file])

  async function submit(event: FormEvent) {
    event.preventDefault(); setError(''); setFieldErrors({}); setSubmitting(true)
    const data = new FormData(); data.set('title', title); data.set('content', content); if (file) data.set('image', file)
    try {
      const post = postId ? await api.updatePost(postId, data) : await api.createPost(data)
      navigate(`/posts/${post.id}`)
    } catch (err) {
      if (err instanceof ApiError) setFieldErrors(err.fieldErrors)
      setError(err instanceof Error ? err.message : 'Unable to publish story.')
    } finally { setSubmitting(false) }
  }

  if (loading) return <div className="page-container"><Loading label="Preparing editor" /></div>
  return (
    <div className="simple-form-page post-form-page">
      <h2>{editing ? 'Edit Post' : 'Add New Post'}</h2>
      <form className="simple-form" onSubmit={submit} encType="multipart/form-data">
        <label>Title<input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Title" maxLength={200} required />{fieldErrors.title && <small>{fieldErrors.title}</small>}</label>
        <label>Content<textarea value={content} onChange={(event) => setContent(event.target.value)} placeholder="Content" rows={9} required />{fieldErrors.content && <small>{fieldErrors.content}</small>}</label>
        {(preview || existingImage) && <div className="current-image"><p>Current image:</p><img src={preview || imageUrl(existingImage!)} alt="Current post" /></div>}
        <label>{editing ? 'Choose a new image (optional)' : 'Image (optional)'}<input type="file" accept="image/jpeg,image/png,image/gif,image/webp" onChange={(event) => setFile(event.target.files?.[0] ?? null)} /></label>
        <div className="form-actions">
          <button className="button" disabled={submitting}>{submitting ? 'Please wait...' : editing ? 'Update' : 'Submit'}</button>
          <Link className="button" to={postId ? `/posts/${postId}` : '/'}>Cancel</Link>
        </div>
      </form>
      {error && <p className="form-error">{error}</p>}
    </div>
  )
}
