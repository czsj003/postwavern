import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, imageUrl } from '../api'
import { useAuth } from '../auth'
import { Loading } from '../components/Loading'
import type { Post, Waver } from '../types'

export function PostDetailPage() {
  const { id } = useParams()
  const postId = Number(id)
  const [post, setPost] = useState<Post | null>(null)
  const [wavers, setWavers] = useState<Waver[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [waveBusy, setWaveBusy] = useState(false)
  const { user, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const load = useCallback(async () => {
    if (!Number.isFinite(postId)) { setError('That story does not exist.'); setLoading(false); return }
    try {
      const [story, people] = await Promise.all([api.post(postId), api.wavers(postId)])
      setPost(story); setWavers(people)
    } catch (err) { setError(err instanceof Error ? err.message : 'Unable to load this story.') }
    finally { setLoading(false) }
  }, [postId])

  useEffect(() => { void load() }, [load])

  async function toggleWave() {
    if (!isAuthenticated) { navigate('/login', { state: { from: `/posts/${postId}` } }); return }
    if (!post || waveBusy) return
    setWaveBusy(true)
    try {
      const result = await api.toggleWave(post.id)
      setPost({ ...post, waveCount: result.count, wavedByCurrentUser: result.waved })
      setWavers(await api.wavers(post.id))
    } finally { setWaveBusy(false) }
  }

  async function deletePost() {
    if (!post || !window.confirm('Delete this story permanently?')) return
    await api.deletePost(post.id)
    navigate('/')
  }

  if (loading) return <div className="page-container"><Loading label="Opening story" /></div>
  if (error || !post) return <div className="page-container"><div className="empty-state"><h2>Story unavailable</h2><p>{error}</p><Link className="button button-dark" to="/">Back to stories</Link></div></div>

  const ownsPost = user?.id === post.userID
  return (
    <article className="page-content post-detail">
      <h2>Post Detail</h2>
      <h3>{post.title}</h3>
      <p className="post-author">By {post.authorName}</p>
      <div className="post-content">{post.content}</div>
      {post.imageName && <img className="detail-image" src={imageUrl(post.imageName)} alt="Post" />}
      <p>Waves: {post.waveCount}</p>
      {wavers.length > 0 && <p className="waver-list">Waved by: {wavers.map((waver) => waver.name).join(', ')}</p>}
      <div className="post-actions">
        <button className="button" onClick={() => void toggleWave()} disabled={waveBusy}>{post.wavedByCurrentUser ? 'Unwave' : 'Wave'}</button>
        <button className="button" onClick={() => navigate('/')}>Go Back</button>
        {ownsPost && <Link className="button" to={`/posts/${post.id}/edit`}>Edit</Link>}
        {ownsPost && <button className="button danger-button" onClick={() => void deletePost()}>Delete</button>}
      </div>
    </article>
  )
}
