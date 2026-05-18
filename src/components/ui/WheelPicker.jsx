import { useRef, useState, useCallback, useEffect } from 'react'

const ITEM_H  = 44
const VISIBLE = 5

// ── 단일 스크롤 컬럼 ──────────────────────────────────────
function WheelColumn({ items, selectedIndex, onSelect, format }) {
  const ref      = useRef(null)
  const timerRef = useRef(null)
  const [liveIdx, setLiveIdx] = useState(selectedIndex)

  // selectedIndex 외부 변경 시 스크롤 동기화
  useEffect(() => {
    const el = ref.current
    if (!el) return
    el.scrollTo({ top: selectedIndex * ITEM_H, behavior: 'smooth' })
    setLiveIdx(selectedIndex)
  }, [selectedIndex])

  const handleScroll = useCallback(() => {
    const el = ref.current
    if (!el) return
    const rawIdx = el.scrollTop / ITEM_H
    setLiveIdx(rawIdx) // 스크롤 중 실시간 시각 반응
    clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => {
      const snapped  = Math.round(el.scrollTop / ITEM_H)
      const clamped  = Math.max(0, Math.min(items.length - 1, snapped))
      el.scrollTo({ top: clamped * ITEM_H, behavior: 'smooth' })
      onSelect(clamped)
    }, 120)
  }, [items.length, onSelect])

  return (
    <div className="relative flex-1 select-none">
      {/* 상단 페이드 */}
      <div className="absolute inset-x-0 top-0 z-10 pointer-events-none rounded-t-xl"
        style={{ height: 2 * ITEM_H, background: 'linear-gradient(to bottom, white 20%, transparent)' }} />
      {/* 하단 페이드 */}
      <div className="absolute inset-x-0 bottom-0 z-10 pointer-events-none rounded-b-xl"
        style={{ height: 2 * ITEM_H, background: 'linear-gradient(to top, white 20%, transparent)' }} />
      {/* 중앙 선택 하이라이트 */}
      <div
        className="absolute inset-x-1 z-10 pointer-events-none rounded-lg bg-navy/8 border border-navy/10"
        style={{ top: 2 * ITEM_H, height: ITEM_H }}
      />
      <div
        ref={ref}
        onScroll={handleScroll}
        style={{
          height: VISIBLE * ITEM_H,
          overflowY: 'scroll',
          scrollSnapType: 'y mandatory',
          scrollbarWidth: 'none',
          msOverflowStyle: 'none',
        }}
      >
        <div style={{ height: 2 * ITEM_H }} />
        {items.map((item, i) => {
          const dist = Math.abs(i - liveIdx)
          const opacity = dist <= 0.3 ? 1 : dist <= 1.3 ? 0.38 : 0.12
          const bold    = dist <= 0.3
          const size    = dist <= 0.3 ? '1.1rem' : dist <= 1.3 ? '0.875rem' : '0.75rem'
          return (
            <div
              key={i}
              onClick={() => {
                onSelect(i)
                ref.current?.scrollTo({ top: i * ITEM_H, behavior: 'smooth' })
              }}
              style={{
                height: ITEM_H,
                scrollSnapAlign: 'center',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                opacity,
                fontWeight: bold ? 700 : 400,
                fontSize: size,
                color: bold ? '#1B2B48' : '#9CA3AF',
                transition: 'font-size 0.12s, opacity 0.12s',
                cursor: 'pointer',
              }}
            >
              {format ? format(item) : item}
            </div>
          )
        })}
        <div style={{ height: 2 * ITEM_H }} />
      </div>
    </div>
  )
}

// ── 데이터 ────────────────────────────────────────────────
const AMPM_LIST = ['오전', '오후']
const HOUR_LIST = Array.from({ length: 12 }, (_, i) => i + 1)         // 1–12
const MIN_LIST  = Array.from({ length: 12 }, (_, i) => i * 5)         // 0,5,...55

function parseTime(str) {
  if (!str) return { ai: 0, hi: 7, mi: 0 }  // 기본값 08:00
  const [h, m] = str.split(':').map(Number)
  const ispm   = h >= 12
  const h12    = h % 12 || 12
  return {
    ai: ispm ? 1 : 0,
    hi: Math.max(0, HOUR_LIST.indexOf(h12)),
    mi: Math.max(0, MIN_LIST.indexOf(m >= 0 ? Math.round(m / 5) * 5 : 0)),
  }
}

function toTime24(ai, hi, mi) {
  const h12  = HOUR_LIST[hi]
  const min  = MIN_LIST[mi]
  const ispm = ai === 1
  const h24  = ispm ? (h12 === 12 ? 12 : h12 + 12) : (h12 === 12 ? 0 : h12)
  return `${String(h24).padStart(2, '0')}:${String(min).padStart(2, '0')}`
}

// ── 메인 WheelPicker ──────────────────────────────────────
export default function WheelPicker({ value, onChange, label = '시간 선택' }) {
  const { ai: ia, hi: ih, mi: im } = parseTime(value)
  const [ai, setAi] = useState(ia)
  const [hi, setHi] = useState(ih)
  const [mi, setMi] = useState(im)
  const [open, setOpen] = useState(false)
  const containerRef = useRef(null)

  // 외부 클릭 시 닫기
  useEffect(() => {
    function handler(e) {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  // 선택이 바뀔 때마다 onChange 호출
  const prevRef = useRef(value)
  useEffect(() => {
    const t = toTime24(ai, hi, mi)
    if (t !== prevRef.current) {
      prevRef.current = t
      onChange(t)
    }
  }, [ai, hi, mi])

  // 트리거 버튼에 표시할 텍스트
  const displayText = value
    ? (() => {
        const [h, m] = value.split(':').map(Number)
        const ap  = h >= 12 ? '오후' : '오전'
        const h12 = h % 12 || 12
        return `${ap} ${h12}:${String(m).padStart(2, '0')}`
      })()
    : label

  return (
    <div ref={containerRef} className="relative">
      {/* 트리거 */}
      <button
        type="button"
        onClick={() => setOpen(v => !v)}
        className={`w-full flex items-center gap-2 px-4 py-3 rounded-xl border text-sm font-semibold transition-all
          ${value
            ? 'border-navy bg-navy/5 text-navy'
            : 'border-offwhite-200 text-gray-400 hover:border-navy'
          } ${open ? 'border-navy ring-2 ring-navy/10' : ''}`}
      >
        <span className="text-base">🕐</span>
        <span>{displayText}</span>
      </button>

      {/* 피커 패널 */}
      {open && (
        <div className="absolute left-0 top-full mt-2 z-50 bg-white rounded-2xl border border-offwhite-200 shadow-2xl p-4"
          style={{ width: 220 }}>
          <p className="text-[11px] font-bold text-gray-400 text-center mb-2 uppercase tracking-widest">{label}</p>

          <div className="flex gap-0.5 rounded-xl overflow-hidden bg-offwhite-100 p-1">
            <WheelColumn items={AMPM_LIST} selectedIndex={ai} onSelect={setAi} />
            <WheelColumn items={HOUR_LIST} selectedIndex={hi} onSelect={setHi} />
            <div className="flex items-center justify-center text-navy font-black text-xl w-4 shrink-0">:</div>
            <WheelColumn
              items={MIN_LIST}
              selectedIndex={mi}
              onSelect={setMi}
              format={m => String(m).padStart(2, '0')}
            />
          </div>

          <button
            type="button"
            onClick={() => setOpen(false)}
            className="mt-3 w-full py-2 rounded-xl bg-navy text-white text-sm font-bold hover:bg-navy/90 transition-colors"
          >
            확인
          </button>
        </div>
      )}
    </div>
  )
}
