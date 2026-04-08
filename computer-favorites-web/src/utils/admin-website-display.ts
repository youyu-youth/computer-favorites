export const toDisplayValue = (value: unknown): string => {
  if (value === null || value === undefined) {
    return '-'
  }
  const text = String(value).trim()
  return text ? text : '-'
}

export const formatDateTime = (value?: string): string => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

export const resolveWebsiteStatusText = (status?: number): string => {
  if (status === 1) {
    return '已上架'
  }
  if (status === 0) {
    return '已下架'
  }
  return '-'
}

export const resolveWebsiteAuditStatusText = (auditStatus?: number): string => {
  if (auditStatus === 1) {
    return '已通过'
  }
  if (auditStatus === 2) {
    return '已拒绝'
  }
  if (auditStatus === 0) {
    return '待审核'
  }
  return '-'
}

export const resolveWebsiteSourceText = (source?: number): string => {
  if (source === 0) {
    return '管理员录入'
  }
  if (source === 1) {
    return '用户投稿'
  }
  return '-'
}

export const resolveDeletedText = (deleted?: number): string => {
  if (deleted === 1) {
    return '已删除'
  }
  if (deleted === 0) {
    return '正常'
  }
  return '-'
}

export const resolveBooleanFlagText = (value?: number): string => {
  if (value === 1) {
    return '是'
  }
  if (value === 0) {
    return '否'
  }
  return '-'
}
