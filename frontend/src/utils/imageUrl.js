/**
 * Build image display URL from a stored relative path.
 *
 * FileController stores uploaded images as "pets/<uuid>.<ext>".
 * The MvcConfig serves "/uploads/**" from the "uploads/" directory.
 * Some manually-inserted records may store just the filename without
 * the "pets/" prefix, so we normalize both cases here.
 */
export function getImageUrl(url) {
  if (!url) return ''
  // Absolute URLs (e.g. https://...) — return unchanged
  if (/^https?:\/\//i.test(url)) return url
  // Blob / data URIs — return unchanged
  if (url.startsWith('blob:') || url.startsWith('data:')) return url
  // Already includes the upload directory prefix — prepend /uploads/
  if (url.startsWith('pets/')) return '/uploads/' + url
  // Bare filename — use /uploads/pets/ as default prefix
  return '/uploads/pets/' + url
}
