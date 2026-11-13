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
  const { isAuthenticated, user } = useAuth()

  const loadPosts = useCallback(async () => {
    setLoading(true)
    setError('')
    try { setPosts(await api.posts(popular)) }
    catch (err) { setError(err instanceof Error ? err.message : 'Unable to load stories.') }
    finally { setLoading(false) }
  }, [popular])

  useEffect(() => { void loadPosts() }, [loadPosts])

  async function deletePost(id: number) {
    if (!window.confirm('Delete this post?')) return
    try {
      await api.deletePost(id)
      setPosts((current) => current.filter((post) => post.id !== id))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to delete post.')
    }
  }

  return (
    <section className="page-content">
      <div className="page-heading">
        <h2>All Posts</h2>
        <div className="sort-controls" role="group" aria-label="Sort posts">
          <button className={!popular ? 'active' : ''} onClick={() => setPopular(false)}>Recent</button>
          <button className={popular ? 'active' : ''} onClick={() => setPopular(true)}>Popular</button>
        </div>
      </div>
      <Link to={isAuthenticated ? '/write' : '/login'} className="button add-post-button">Add New Post</Link>

      {loading && <Loading label="Loading posts..." />}
      {error && <div className="notice error"><p>{error}</p><button onClick={() => void loadPosts()}>Try again</button></div>}
      {!loading && !error && posts.length === 0 && <p className="empty-message">No posts found.</p>}
      {!loading && posts.length > 0 && <div className="post-list">
        {posts.map((post) => <PostCard key={post.id} post={post} canManage={user?.id === post.userID} onDelete={() => void deletePost(post.id)} />)}
      </div>}
    </section>
  )
}
