import { ArrowRight, PenLine } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { Loading } from '../components/Loading'
import { PostCard } from '../components/PostCard'
import type { Post } from '../types'

export function HomePage() {
  const [posts, setPosts] = useState<Post[]>([])
  const [popular, setPopular] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { isAuthenticated } = useAuth()

  const loadPosts = useCallback(async () => {
    setLoading(true)
    setError('')
    try { setPosts(await api.posts(popular)) }
    catch (err) { setError(err instanceof Error ? err.message : 'Unable to load stories.') }
    finally { setLoading(false) }
  }, [popular])

  useEffect(() => { void loadPosts() }, [loadPosts])

  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <span className="eyebrow">Independent writing, thoughtfully shared</span>
          <h1>Ideas worth<br />reading.</h1>
          <p>A quiet place to publish useful thoughts, discover clear perspectives, and support writing that stays with you.</p>
          <div className="hero-actions">
            <Link to={isAuthenticated ? '/write' : '/signup'} className="button button-accent">
              <PenLine size={18} /> Start writing
            </Link>
            <a href="#stories" className="button button-ghost">Explore stories <ArrowRight size={18} /></a>
          </div>
        </div>
        <div className="hero-note" aria-hidden="true">
          <span>POSTWAVE / 01</span>
          <p>Write with clarity.<br />Read with intention.</p>
          <div className="hero-rule" />
        </div>
      </section>

      <section className="content-section" id="stories">
        <div className="section-heading">
          <div><span className="section-kicker">LATEST STORIES</span><h2>From the community</h2></div>
          <div className="segmented" role="group" aria-label="Sort stories">
            <button className={!popular ? 'active' : ''} onClick={() => setPopular(false)}>Recent</button>
            <button className={popular ? 'active' : ''} onClick={() => setPopular(true)}>Popular</button>
          </div>
        </div>

        {loading && <Loading />}
        {error && <div className="notice error"><p>{error}</p><button onClick={() => void loadPosts()}>Try again</button></div>}
        {!loading && !error && posts.length === 0 && (
          <div className="empty-state"><div className="empty-mark">~</div><h3>No stories yet</h3><p>Be the first person to send an idea into the world.</p><Link to={isAuthenticated ? '/write' : '/signup'} className="button button-dark">Write the first story</Link></div>
        )}
        {!loading && posts.length > 0 && (
          <div className="post-grid">
            {posts.map((post, index) => <PostCard key={post.id} post={post} featured={index === 0 && posts.length > 2} />)}
          </div>
        )}
      </section>
    </>
  )
}
