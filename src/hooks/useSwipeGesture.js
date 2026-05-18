import { useRef, useCallback } from 'react'

/**
 * 모바일 스와이프로 사이드바를 열고 닫는 제스처 핸들러를 반환합니다.
 *
 * - 오른쪽 스와이프 (화면 왼쪽 끝 40px 이내에서 시작, 50px 이상 이동) → onOpen
 * - 왼쪽 스와이프 (사이드바가 열린 상태에서 50px 이상 이동) → onClose
 */
export function useSwipeGesture({ isOpen, onOpen, onClose }) {
  const touchStart = useRef({ x: 0, y: 0 })

  const handleTouchStart = useCallback((e) => {
    touchStart.current = {
      x: e.touches[0].clientX,
      y: e.touches[0].clientY,
    }
  }, [])

  const handleTouchEnd = useCallback((e) => {
    const dx = e.changedTouches[0].clientX - touchStart.current.x
    const dy = e.changedTouches[0].clientY - touchStart.current.y

    // 수평 스와이프인지 확인 (세로 이동이 가로 이동보다 작아야 함)
    if (Math.abs(dy) > Math.abs(dx)) return

    const THRESHOLD = 50 // px

    if (dx > THRESHOLD && !isOpen && touchStart.current.x < 40) {
      // 왼쪽 끝에서 오른쪽으로 스와이프 → 열기
      onOpen()
    } else if (dx < -THRESHOLD && isOpen) {
      // 왼쪽으로 스와이프 → 닫기
      onClose()
    }
  }, [isOpen, onOpen, onClose])

  return { handleTouchStart, handleTouchEnd }
}
