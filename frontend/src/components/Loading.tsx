export function Loading({ label = 'Loading stories' }: { label?: string }) {
  return <div className="loading"><span className="loader" /><span>{label}</span></div>
}
