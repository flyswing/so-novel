const Api = {

  _handle(r) {
    if (r.status === 401) {
      const path = location.pathname || ''
      if (!path.endsWith('/login.html')) {
        location.href = '/login.html'
      }
      return Promise.reject(new Error('unauthorized'))
    }
    return r
  },

  getConfig() {
    return fetch('/config', { credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

  getLocalBooks() {
    return fetch('/local-books', { credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

  search(keyword) {
    return fetch(`/search/aggregated?kw=${encodeURIComponent(keyword)}`, { credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

  downloadBook(params) {
    return fetch(`/book-fetch?${params.toString()}`, { credentials: 'same-origin' })
      .then(r => this._handle(r))
  },

  deleteBook(filename) {
    return fetch(`/book-delete?filename=${encodeURIComponent(filename)}`, { credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

  getSuggestions(kw) {
    return fetch(`/suggestion?kw=${encodeURIComponent(kw)}`, { credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

  getSources() {
    return fetch('/sources', { credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

  checkSources() {
    return fetch('/sources/check', { credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

  logout() {
    return fetch('/logout', { method: 'POST', credentials: 'same-origin' })
      .then(r => this._handle(r))
      .then(r => r.json())
  },

}
