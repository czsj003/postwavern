import { ArrowLeft, ImagePlus, Send, X } from 'lucide-react'
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
    <div className="editor-page">
      <div className="editor-topbar"><Link to={postId ? `/posts/${postId}` : '/'} className="back-link"><ArrowLeft size={18} /> Cancel</Link><span>{editing ? 'EDIT STORY' : 'NEW STORY'}</span></div>
      <form className="editor-form" onSubmit={submit}>
        <div className="editor-intro"><span className="section-kicker">{editing ? 'REFINE YOUR STORY' : 'SHARE AN IDEA'}</span><h1>{editing ? 'Edit your story' : 'What’s on your mind?'}</h1><p>Give your idea room to breathe. Clear, honest writing travels furthest.</p></div>
        {error && <div className="form-alert">{error}</div>}
        <label className="editor-label">Title <span>{title.length}/200</span><input className="title-input" value={title} onChange={(event) => setTitle(event.target.value)} placeholder="A title people will remember" maxLength={200} required /></label>
        {fieldErrors.title && <small className="field-error">{fieldErrors.title}</small>}
        <label className="editor-label">Story<textarea className="content-input" value={content} onChange={(event) => setContent(event.target.value)} placeholder="Tell your story…" rows={12} required /></label>
        {fieldErrors.content && <small className="field-error">{fieldErrors.content}</small>}
        <div className="image-field">
          <div><strong>Cover image</strong><p>JPEG, PNG, GIF or WebP · up to 5 MB</p></div>
          {(preview || existingImage) ? <div className="image-preview"><img src={preview || imageUrl(existingImage!)} alt="Cover preview" /><label className="replace-image"><ImagePlus size={17} /> Replace<input type="file" accept="image/jpeg,image/png,image/gif,image/webp" onChange={(event) => setFile(event.target.files?.[0] ?? null)} /></label>{preview && <button type="button" onClick={() => setFile(null)} aria-label="Remove new image"><X /></button>}</div> : <label className="image-drop"><ImagePlus /><span>Add a cover image</span><small>Choose from your device</small><input type="file" accept="image/jpeg,image/png,image/gif,image/webp" onChange={(event) => setFile(event.target.files?.[0] ?? null)} /></label>}
        </div>
        <div className="editor-submit"><span>Your story will be visible to everyone.</span><button className="button button-accent" disabled={submitting}><Send size={17} /> {submitting ? 'Publishing…' : editing ? 'Save changes' : 'Publish story'}</button></div>
      </form>
    </div>
  )
}
