/**
 * Breed name mapping: English → Chinese.
 * Covers seed data breeds. New entries added here as needed.
 */
export const breedLabel = {
  'British Shorthair': '英短',
  'Persian': '波斯',
  'Siamese': '暹罗',
  'Maine Coon': '缅因',
  'Ragdoll': '布偶',
  'Golden Retriever': '金毛',
  'Corgi': '柯基',
  'Husky': '哈士奇',
  'Labrador': '拉布拉多',
  'Poodle': '贵宾',
  'Holland Lop': '荷兰垂耳兔',
  'Netherland Dwarf': '荷兰侏儒兔',
  'Mini Rex': '迷你雷克斯兔',
}

/**
 * Resolve breed display name — uses mapping when available, falls back to raw value.
 */
export function getBreedDisplay(breedName) {
  if (!breedName) return '未知品种'
  return breedLabel[breedName] || breedName
}

/**
 * Pet status mapping: value → Chinese label.
 */
export const petStatusLabel = {
  available: '可领养',
  adopted: '已领养',
  pending: '待审核',
}

/** Resolve pet status display name. */
export function getPetStatusDisplay(status) {
  if (!status) return '未知'
  return petStatusLabel[status] || status
}

/** Pet status → Element Plus tag type. */
export function getPetStatusTagType(status) {
  const m = { available: 'success', adopted: 'warning', pending: 'info' }
  return m[status] || 'info'
}

/**
 * Application status mapping.
 */
export const appStatusLabel = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
  cancelled: '已取消',
}

/** Resolve application status display name. */
export function getAppStatusDisplay(status) {
  if (!status) return '未知'
  return appStatusLabel[status] || status
}

/** Application status → Element Plus tag type. */
export function getAppStatusTagType(status) {
  const m = { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }
  return m[status] || 'info'
}
