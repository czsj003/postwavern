import { ArrowLeft, Edit3, Heart, Share2, Trash2 } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, imageUrl } from '../api'
import { useAuth } from '../auth'
import { Loading } from '../components/Loading'
import type { Post, Waver } from '../types'
import { formatDate, initials, readingTime } from '../utils'

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

  async function share() {
    const data = { title: post?.title ?? 'PostWave', url: window.location.href }
    if (navigator.share) await navigator.share(data)
    else { await navigator.clipboard.writeText(window.location.href); window.alert('Link copied to clipboard.') }
  }

  if (loading) return <div className="page-container"><Loading label="Opening story" /></div>
  if (error || !post) return <div className="page-container"><div className="empty-state"><h2>Story unavailable</h2><p>{error}</p><Link className="button button-dark" to="/">Back to stories</Link></div></div>

  const ownsPost = user?.id === post.userID
  return (
    <article className="story-page">
      <div className="story-topbar">
        <Link to="/" className="back-link"><ArrowLeft size={18} /> All stories</Link>
        {ownsPost && <div className="story-actions"><Link to={`/posts/${post.id}/edit`} className="icon-text"><Edit3 size={17} /> Edit</Link><button className="icon-text danger" onClick={() => void deletePost()}><Trash2 size={17} /> Delete</button></div>}
      </div>
      <header className="story-header">
        <span className="section-kicker">COMMUNITY STORY</span>
        <h1>{post.title}</h1>
        <div className="story-byline">
          <span className="avatar">{initials(post.authorName)}</span>
          <div><strong>{post.authorName}</strong><span>{formatDate(post.createdAt)} · {readingTime(post.content)} min read</span></div>
        </div>
      </header>
      {post.imageName && <div className="story-hero-image"><img src={imageUrl(post.imageName)} alt="" /></div>}
      <div className="story-layout">
        <aside className="story-rail">
          <button className={post.wavedByCurrentUser ? 'rail-action active' : 'rail-action'} onClick={() => void toggleWave()} disabled={waveBusy} aria-label="Wave for this story"><Heart fill={post.wavedByCurrentUser ? 'currentColor' : 'none'} /><span>{post.waveCount}</span></button>
          <button className="rail-action" onClick={() => void share()} aria-label="Share story"><Share2 /><span>Share</span></button>
        </aside>
        <div className="story-content">
          {post.content.split(/\n{2,}/).map((paragraph, index) => <p key={index}>{paragraph}</p>)}
          <div className="story-end">~</div>
          <div className="wave-box">
            <button className={post.wavedByCurrentUser ? 'big-wave active' : 'big-wave'} onClick={() => void toggleWave()} disabled={waveBusy}><Heart fill={post.wavedByCurrentUser ? 'currentColor' : 'none'} /> {post.wavedByCurrentUser ? 'You waved' : 'Wave for this story'}</button>
            <div>{wavers.length ? <><strong>{wavers.length} {wavers.length === 1 ? 'person' : 'people'}</strong> sent this story forward<div className="avatar-stack">{wavers.slice(0, 5).map((waver) => <span className="avatar avatar-small" key={waver.id} title={waver.name}>{initials(waver.name)}</span>)}</div></> : <span>Be the first to wave.</span>}</div>
          </div>
        </div>
      </div>
    </article>
  )
}
