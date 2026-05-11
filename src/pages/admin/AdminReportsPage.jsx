import { useState, useMemo } from 'react'
import { Flag, AlertTriangle, CheckCircle2, X, Search } from 'lucide-react'
import { ADMIN_REPORTS } from '../../data/mockAdmin'

const STORAGE_KEY = 'staffnow_admin_reports_status'

const TYPE_COLOR = {
  '허위 정보':   'bg-red-50 text-red-600 border-red-200',
  '비매너 행동': 'bg-orange-50 text-orange border-orange/30',
}

const TARGET_TYPE_COLOR = {
  '공고':        'bg-blue-50 text-blue-600',
  '기업 담당자': 'bg-purple-50 text-purple-600',
}

function loadStatus() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') } catch { return {} }
}

function saveStatus(obj) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(obj)) } catch {}
}

const TABS = [
  { key: 'all',      label: '전체' },
  { key: 'pending',  label: '대기 중' },
  { key: 'resolved', label: '처리 완료' },
]

export default function AdminReportsPage() {
  const [statusMap, setStatusMap]       = useState(loadStatus)
  const [tab, setTab]                   = useState('all')
  const [query, setQuery]               = useState('')
  const [viewReport, setViewReport]     = useState(null)
  const [resolveTarget, setResolveTarget] = useState(null)
  const [memo, setMemo]                 = useState('')

  const reports = useMemo(() =>
    ADMIN_REPORTS.map(r => ({
      ...r,
      ...(statusMap[r.id] ?? {}),
      status: statusMap[r.id]?.status ?? 'pending',
    }))
  , [statusMap])

  const filtered = useMemo(() => {
    const q = query.toLowerCase()
    return reports
      .filter(r => tab === 'all' || r.status === tab)
      .filter(r => !q || r.target.toLowerCase().includes(q) || r.type.toLowerCase().includes(q) || r.reporter.toLowerCase().includes(q))
  }, [reports, tab, query])

  const pendingCount  = reports.filter(r => r.status === 'pending').length
  const resolvedCount = reports.filter(r => r.status === 'resolved').length

  function openResolve(r) {
    setResolveTarget(r)
    setMemo('')
  }

  function confirmResolve() {
    const next = {
      ...statusMap,
      [resolveTarget.id]: {
        status: 'resolved',
        memo: memo.trim() || null,
        resolvedAt: new Date().toISOString().slice(0, 10),
      },
    }
    setStatusMap(next)
    saveStatus(next)
    // 상세 보기 중이면 갱신
    if (viewReport?.id === resolveTarget.id) setViewReport({ ...viewReport, status: 'resolved', memo: memo.trim() || null })
    setResolveTarget(null)
  }

  function undo(id) {
    const next = { ...statusMap }
    delete next[id]
    setStatusMap(next)
    saveStatus(next)
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">신고/제재</h1>
        <p className="text-sm text-gray-500 mt-1">
          대기 중 <strong className="text-red-500">{pendingCount}건</strong>
          {resolvedCount > 0 && <span className="ml-2 text-gray-400">· 처리 완료 {resolvedCount}건</span>}
        </p>
      </div>

      {/* 검색 + 탭 */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-xl px-4 py-2.5 flex-1 max-w-sm">
          <Search size={15} className="text-gray-400 shrink-0" />
          <input
            type="text"
            placeholder="대상 / 유형 / 신고자 검색..."
            value={query}
            onChange={e => setQuery(e.target.value)}
            className="bg-transparent text-sm outline-none w-full placeholder-gray-400 text-navy"
          />
        </div>
        <div className="flex gap-1 bg-offwhite-100 rounded-xl p-1 self-start">
          {TABS.map(t => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-colors ${
                tab === t.key ? 'bg-white text-navy shadow-sm' : 'text-gray-500 hover:text-navy'
              }`}
            >
              {t.label}
              {t.key === 'pending' && pendingCount > 0 && (
                <span className="ml-1.5 bg-red-500 text-white text-[10px] font-bold w-4 h-4 rounded-full inline-flex items-center justify-center">
                  {pendingCount}
                </span>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* 리스트 */}
      {filtered.length === 0 ? (
        <div className="bg-white rounded-2xl border border-offwhite-200 py-16 text-center text-sm text-gray-400">
          <Flag size={32} className="mx-auto mb-3 text-gray-300" />
          {query ? '검색 결과가 없습니다' : '신고 내역이 없습니다'}
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map(r => {
            const isPending = r.status === 'pending'
            return (
              <div
                key={r.id}
                className={`bg-white rounded-2xl border p-5 transition-colors ${
                  isPending ? 'border-red-200' : 'border-offwhite-200'
                }`}
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-start gap-3 min-w-0">
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 mt-0.5 ${isPending ? 'bg-red-50' : 'bg-green-50'}`}>
                      {isPending
                        ? <AlertTriangle size={18} className="text-red-500" />
                        : <CheckCircle2  size={18} className="text-green-500" />
                      }
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-bold text-navy">{r.target}</p>
                        {r.targetType && (
                          <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${TARGET_TYPE_COLOR[r.targetType] ?? 'bg-gray-100 text-gray-500'}`}>
                            {r.targetType}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-2 mt-1 flex-wrap">
                        <span className={`text-[11px] font-semibold px-2 py-0.5 rounded-full border ${TYPE_COLOR[r.type] ?? 'bg-gray-50 text-gray-500 border-gray-200'}`}>
                          {r.type}
                        </span>
                        <p className="text-xs text-gray-400">신고자: {r.reporter} · {r.reportedAt}</p>
                      </div>
                      {!isPending && r.memo && (
                        <p className="text-xs text-gray-500 mt-1.5 bg-gray-50 rounded-lg px-3 py-1.5 border border-gray-100">
                          처리 메모: {r.memo}
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-col items-end gap-2 shrink-0">
                    {isPending ? (
                      <>
                        <button
                          onClick={() => openResolve(r)}
                          className="text-sm font-semibold bg-navy text-white px-3.5 py-1.5 rounded-xl hover:bg-navy/80 transition-colors"
                        >
                          처리 완료
                        </button>
                        <button
                          onClick={() => setViewReport(r)}
                          className="text-xs text-gray-400 hover:text-navy underline underline-offset-2 transition-colors"
                        >
                          상세 보기
                        </button>
                      </>
                    ) : (
                      <>
                        <span className="text-xs font-bold text-green-600 bg-green-50 px-3 py-1.5 rounded-full">처리됨</span>
                        <div className="flex gap-2">
                          <button
                            onClick={() => setViewReport(r)}
                            className="text-xs text-gray-400 hover:text-navy underline underline-offset-2 transition-colors"
                          >
                            상세 보기
                          </button>
                          <span className="text-gray-200">|</span>
                          <button
                            onClick={() => undo(r.id)}
                            className="text-xs text-gray-400 hover:text-navy underline underline-offset-2 transition-colors"
                          >
                            되돌리기
                          </button>
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      )}

      {/* 상세 보기 모달 */}
      {viewReport && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setViewReport(null)}
        >
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
              <h3 className="font-bold text-navy">신고 상세</h3>
              <button onClick={() => setViewReport(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="p-5 space-y-4">
              {[
                { label: '신고 대상',  value: viewReport.target },
                { label: '대상 유형',  value: viewReport.targetType ?? '—' },
                { label: '신고 유형',  value: viewReport.type },
                { label: '신고자',     value: `${viewReport.reporter} (${viewReport.reporterEmail ?? '—'})` },
                { label: '신고일',     value: viewReport.reportedAt },
                { label: '처리 상태',  value: viewReport.status === 'pending' ? '대기 중' : `처리 완료 (${viewReport.resolvedAt ?? ''})` },
              ].map(({ label, value }) => (
                <div key={label} className="flex gap-3">
                  <span className="text-xs font-semibold text-gray-400 w-20 shrink-0 mt-0.5">{label}</span>
                  <span className="text-sm text-navy flex-1">{value}</span>
                </div>
              ))}

              {viewReport.detail && (
                <div>
                  <p className="text-xs font-semibold text-gray-400 mb-1.5">신고 내용</p>
                  <p className="text-sm text-gray-700 bg-gray-50 rounded-xl px-4 py-3 border border-gray-100 leading-relaxed">
                    {viewReport.detail}
                  </p>
                </div>
              )}

              {viewReport.status === 'resolved' && viewReport.memo && (
                <div>
                  <p className="text-xs font-semibold text-gray-400 mb-1.5">처리 메모</p>
                  <p className="text-sm text-gray-700 bg-green-50 rounded-xl px-4 py-3 border border-green-100 leading-relaxed">
                    {viewReport.memo}
                  </p>
                </div>
              )}
            </div>
            <div className="px-5 pb-5 flex gap-2">
              {viewReport.status === 'pending' && (
                <button
                  onClick={() => { openResolve(viewReport); setViewReport(null) }}
                  className="flex-1 py-2.5 text-sm font-semibold text-white bg-navy hover:bg-navy/80 rounded-xl transition-colors"
                >
                  처리 완료
                </button>
              )}
              <button
                onClick={() => setViewReport(null)}
                className="flex-1 py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:bg-offwhite-100 transition-colors"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 처리 완료 메모 입력 모달 */}
      {resolveTarget && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setResolveTarget(null)}
        >
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4 overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
              <h3 className="font-bold text-navy">처리 완료</h3>
              <button onClick={() => setResolveTarget(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="p-5">
              <p className="text-sm text-gray-600 mb-4">
                <strong className="text-navy">"{resolveTarget.target}"</strong> 신고를 처리 완료 처리합니다.
              </p>
              <textarea
                autoFocus
                rows={3}
                value={memo}
                onChange={e => setMemo(e.target.value)}
                placeholder="처리 메모 입력 (선택) — 예: 공고 내용 수정 요청, 경고 조치 등"
                className="w-full text-sm border border-offwhite-200 rounded-xl px-4 py-3 resize-none focus:outline-none focus:border-navy transition-colors"
              />
            </div>
            <div className="px-5 pb-5 flex gap-2">
              <button
                onClick={() => setResolveTarget(null)}
                className="flex-1 py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:bg-offwhite-100 transition-colors"
              >
                취소
              </button>
              <button
                onClick={confirmResolve}
                className="flex-1 py-2.5 text-sm font-semibold text-white bg-navy hover:bg-navy/80 rounded-xl transition-colors"
              >
                처리 완료
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
