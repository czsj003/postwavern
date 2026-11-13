import { ArrowUpRight, Clock3, Heart } from 'lucide-react'
import { Link } from 'react-router-dom'
import { imageUrl } from '../api'
import type { Post } from '../types'
import { formatDate, initials, readingTime } from '../utils'

export function PostCard({ post, featured = false }: { post: Post; featured?: boolean }) {
  return (
    <article className={featured ? 'post-card featured' : 'post-card'}>
      {post.imageName ? (
        <Link to={`/posts/${post.id}`} className="post-image-wrap">
          <img className="post-image" src={imageUrl(post.imageName)} alt="" />
        </Link>
      ) : (
        <Link to={`/posts/${post.id}`} className="post-image-wrap post-image-fallback" aria-label={post.title}>
          <span>PW</span>
        </Link>
      )}
      <div className="post-card-body">
        <div className="author-line">
          <span className="avatar avatar-small">{initials(post.authorName)}</span>
          <span>{post.authorName}</span><span className="dot">·</span><span>{formatDate(post.createdAt)}</span>
        </div>
        <Link to={`/posts/${post.id}`} className="post-title-link"><h2>{post.title}</h2></Link>
        <p className="post-excerpt">{post.content}</p>
        <div className="post-meta">
          <span><Clock3 size={15} /> {readingTime(post.content)} min read</span>
          <span className={post.wavedByCurrentUser ? 'waved' : ''}><Heart size={15} fill={post.wavedByCurrentUser ? 'currentColor' : 'none'} /> {post.waveCount}</span>
          <Link to={`/posts/${post.id}`} className="read-link">Read story <ArrowUpRight size={16} /></Link>
        </div>
      </div>
    </article>
  )
}
