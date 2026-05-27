export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  options?: {
    immediate?: boolean
  }
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let lastInvokeTime = 0
  const isMobile = typeof window !== 'undefined' && ('ontouchstart' in window || navigator.maxTouchPoints > 0)

  const waitTime = isMobile ? Math.max(wait, 300) : wait

  return (...args: Parameters<T>) => {
    const now = Date.now()
    const callImmediately = options?.immediate && lastInvokeTime === 0

    if (timeout) {
      clearTimeout(timeout)
    }

    if (callImmediately) {
      lastInvokeTime = now
      func(...args)
      return
    }

    const remaining = waitTime - (now - lastInvokeTime)
    if (remaining > 0 && remaining < waitTime) {
      timeout = setTimeout(() => {
        lastInvokeTime = Date.now()
        timeout = null
        func(...args)
      }, remaining)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        lastInvokeTime = Date.now()
        timeout = null
        func(...args)
      }, waitTime)
    }
  }
}

export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean
  const isMobile = typeof window !== 'undefined' && ('ontouchstart' in window || navigator.maxTouchPoints > 0)
  const limitTime = isMobile ? Math.max(limit, 200) : limit

  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => {
        inThrottle = false
      }, limitTime)
    }
  }
}