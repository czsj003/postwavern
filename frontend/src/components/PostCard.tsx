import { Link } from 'react-router-dom'
import { imageUrl } from '../api'
import type { Post } from '../types'

export function PostCard({ post, canManage, onDelete }: { post: Post; canManage: boolean; onDelete: () => void }) {
  return (
    <article className="post-preview">
      <h3>{post.title}</h3>
      {post.imageName && <img className="post-preview-image" src={imageUrl(post.imageName)} alt="Post preview" />}
      <p className="post-author">By {post.authorName}</p>
      <p>Waves: {post.waveCount}</p>
      <div className="post-actions">
        <Link className="button button-small" to={`/posts/${post.id}`}>View</Link>
        {canManage && <Link className="button button-small" to={`/posts/${post.id}/edit`}>Edit</Link>}
        {canManage && <button className="button button-small danger-button" onClick={onDelete}>Delete</button>}
      </div>
    </article>
  )
}
