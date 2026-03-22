export class HttpError extends Error {
  public readonly status: number

  public readonly body: unknown

  public constructor(status: number, message: string, body: unknown) {
    super(message)
    this.status = status
    this.body = body
  }
}

const AUTH_UNAUTHORIZED_CODE = 100202

// type ApiErrorBody = {
//   code?: number
//   msg?: string
// }

type UnauthorizedHandler = (error: HttpError) => Promise<void> | void

let unauthorizedHandler: UnauthorizedHandler | null = null
let unauthorizedHandling = false

const isObjectBody = (value: unknown): value is Record<string, unknown> => {
  return typeof value === 'object' && value !== null
}

const getBodyCode = (body: unknown): number | undefined => {
  if (!isObjectBody(body)) {
    return undefined
  }
  const code = body.code
  return typeof code === 'number' ? code : undefined
}

const isUnauthorizedResponse = (status: number, body: unknown): boolean => {
  if (status === 401) {
    return true
  }
  return getBodyCode(body) === AUTH_UNAUTHORIZED_CODE
}

const resolveErrorMessage = (status: number, body: unknown): string => {
  if (typeof body === 'string' && body) {
    return body
  }
  if (isObjectBody(body) && typeof body.msg === 'string' && body.msg) {
    return body.msg
  }
  return `HTTP ${status}`
}

async function emitUnauthorized(error: HttpError): Promise<void> {
  if (!unauthorizedHandler || unauthorizedHandling) {
    return
  }
  unauthorizedHandling = true
  try {
    await unauthorizedHandler(error)
  } finally {
    unauthorizedHandling = false
  }
}

const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken')
  const tokenName = localStorage.getItem('tokenName') || 'satoken'
  const headers = new Headers()
  if (!token) {
    return headers
  }

  headers.set(tokenName, token)
  return headers
}

async function requestJson<T>(url: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  const authHeaders = getAuthHeaders()
  authHeaders.forEach((value, key) => {
    if (!headers.has(key)) {
      headers.set(key, value)
    }
  })

  const res = await fetch(url, {
    ...init,
    headers,
  })

  if (res.status === 204) {
    return undefined as T
  }

  const contentType = res.headers.get('content-type') ?? ''
  const body = contentType.includes('application/json') ? await res.json() : await res.text()

  if (isUnauthorizedResponse(res.status, body)) {
    const error = new HttpError(401, resolveErrorMessage(401, body), body)
    await emitUnauthorized(error)
    throw error
  }

  if (!res.ok) {
    throw new HttpError(res.status, resolveErrorMessage(res.status, body), body)
  }

  return body as T
}

export async function getJson<T>(url: string, init?: RequestInit): Promise<T> {
  return requestJson<T>(url, {
    method: 'GET',
    ...init,
  })
}

export async function postJson<T>(url: string, data: unknown, init?: RequestInit): Promise<T> {
  return requestJson<T>(url, {
    method: 'POST',
    headers: new Headers({
      'Content-Type': 'application/json',
      ...(init?.headers ? Object.fromEntries(new Headers(init.headers).entries()) : {}),
    }),
    body: JSON.stringify(data),
    ...init,
  })
}

export async function putJson<T>(url: string, data?: unknown, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  const hasJsonBody = data !== undefined
  if (hasJsonBody && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  return requestJson<T>(url, {
    method: 'PUT',
    ...init,
    headers,
    body: hasJsonBody ? JSON.stringify(data) : init?.body,
  })
}

export async function deleteJson<T>(url: string, init?: RequestInit): Promise<T> {
  return requestJson<T>(url, {
    method: 'DELETE',
    ...init,
  })
}

export function registerUnauthorizedHandler(handler: UnauthorizedHandler): void {
  unauthorizedHandler = handler
}

export function isUnauthorizedError(error: unknown): error is HttpError {
  return error instanceof HttpError && isUnauthorizedResponse(error.status, error.body)
}
