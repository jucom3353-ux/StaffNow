const BASE = '/api'

const TOKEN_KEY = 'staffnow_access_token'
const REFRESH_KEY = 'staffnow_refresh_token'

export const tokenStorage = {
  getAccess: () => localStorage.getItem(TOKEN_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  set: (access, refresh) => {
    localStorage.setItem(TOKEN_KEY, access)
    localStorage.setItem(REFRESH_KEY, refresh)
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}

async function tryRefresh() {
  const refreshToken = tokenStorage.getRefresh()
  if (!refreshToken) return false

  try {
    const res = await fetch(`${BASE}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })
    if (!res.ok) {
      tokenStorage.clear()
      return false
    }
    const data = await res.json()
    tokenStorage.set(data.accessToken, data.refreshToken)
    return true
  } catch {
    tokenStorage.clear()
    return false
  }
}

export async function request(method, path, body) {
  const token = tokenStorage.getAccess()
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  })

  if (res.status === 401) {
    const refreshed = await tryRefresh()
    if (!refreshed) return res

    return fetch(`${BASE}${path}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${tokenStorage.getAccess()}`,
      },
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
    })
  }

  return res
}

export const authApi = {
  login: (email, password) =>
    fetch(`${BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }),

  refresh: (refreshToken) =>
    fetch(`${BASE}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    }),
}

export const userApi = {
  signup: (data) => request('POST', '/users', data),
}

export const jobApi = {
  myList: () => request('GET', '/job-posts/my'),
  get: (id) => request('GET', `/job-posts/${id}`),
  create: (data) => request('POST', '/job-posts', data),
  update: (id, data) => request('PUT', `/job-posts/${id}`, data),
  updateStatus: (id, status) => request('PATCH', `/job-posts/${id}/status?postStatus=${status}`),
  delete: (id) => request('DELETE', `/job-posts/${id}`),
}

export const jobSearchApi = {
  search: ({ title, workLocation, category, sort, page, size } = {}) => {
    const params = new URLSearchParams()
    if (title) params.set('title', title)
    if (workLocation) params.set('workLocation', workLocation)
    if (category) params.set('category', category)
    if (sort) params.set('sort', sort)
    if (page != null) params.set('page', page)
    if (size != null) params.set('size', size)
    return request('GET', `/job-posts/search?${params}`)
  },
  get: (id) => request('GET', `/job-posts/${id}`),
}

export const applicationApi = {
  apply: (jobPostId) => request('POST', `/applications/${jobPostId}`),
  cancel: (applicationId) => request('DELETE', `/applications/${applicationId}`),
  myList: () => request('GET', '/applications/my'),
  jobApplicants: (jobPostId) => request('GET', `/applications/job-posts/${jobPostId}`),
  approve: (applicationId) => request('PATCH', `/applications/${applicationId}/approve`),
  reject: (applicationId) => request('PATCH', `/applications/${applicationId}/reject`),
  complete: (applicationId) => request('PATCH', `/applications/${applicationId}/complete`),
  noShow: (applicationId) => request('PATCH', `/applications/${applicationId}/no-show`),
}

export const bookmarkApi = {
  add: (jobPostId) => request('POST', `/job-posts/${jobPostId}/bookmark`),
  remove: (jobPostId) => request('DELETE', `/job-posts/${jobPostId}/bookmark`),
  myList: () => request('GET', '/job-posts/bookmarks'),
}

export const contractApi = {
  myList: () => request('GET', '/contracts'),
  get: (contractId) => request('GET', `/contracts/${contractId}`),
  create: (data) => request('POST', '/contracts', data),
  sign: (contractId) => request('PATCH', `/contracts/${contractId}/sign`),
  cancel: (contractId) => request('PATCH', `/contracts/${contractId}/cancel`),
}

export const workAttendanceApi = {
  checkIn: (applicationId) => request('POST', `/work-attendance/${applicationId}/check-in`),
  checkOut: (applicationId) => request('POST', `/work-attendance/${applicationId}/check-out`),
  get: (applicationId) => request('GET', `/work-attendance/${applicationId}`),
}
