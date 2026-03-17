export class HttpError extends Error {
  public readonly status: number

  public readonly body: unknown

  public constructor(status: number, message: string, body: unknown) {
    super(message)
    this.status = status
    this.body = body
  }
}

export async function postJson<T>(url: string, data: unknown, init?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
    body: JSON.stringify(data),
    ...init,
  })

  const contentType = res.headers.get('content-type') ?? ''
  const body = contentType.includes('application/json') ? await res.json() : await res.text()

  if (!res.ok) {
    const msg = typeof body === 'string' && body ? body : `HTTP ${res.status}`
    throw new HttpError(res.status, msg, body)
  }

  return body as T
}

