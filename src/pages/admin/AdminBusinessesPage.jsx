import { useState } from 'react'
import { Building2, CheckCircle2, XCircle, X, AlertTriangle } from 'lucide-react'
import { PENDING_BUSINESSES } from '../../data/mockAdmin'

const STORAGE_KEY = 'staffnow_admin_biz_status'

function loadStatus() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') } catch { return {} }
}

function saveStatus(obj) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(obj)) } catch {}
}

export default function AdminBusinessesPage() {
  const [processed, setProcessed] = useState(loadStatus)
  // 거절 모달 상태: { id, company } | null
  const [rejectTarget, setRejectTarget] = useState(null)
  const [rejectReason, setRejectReason] = useState('')
  const [reasonError, setReasonError]   = useState('')

  const pending   = PENDING_BUSINESSES.filter(b => !processed[b.id])
  const done      = PENDING_BUSINESSES.filter(b =>  processed[b.id])

  function approve(id) {
    const next = {
      ...processed,
      [id]: { action: 'approved', processedAt: new Date().toISOString().slice(0, 10) },
    }
    setProcessed(next)
    saveStatus(next)
  }

  function openReject(b) {
    setRejectTarget(b)
    setRejectReason('')
    setReasonError('')
  }

  function confirmReject() {
    if (!rejectReason.trim()) {
      setReasonError('반려 사유를 입력해 주세요')
      return
    }
    const next = {
      ...processed,
      [rejectTarget.id]: {
        action: 'rejected',
        reason: rejectReason.trim(),
        processedAt: new Date().toISOString().slice(0, 10),
      },
    }
    setProcessed(next)
    saveStatus(next)
    setRejectTarget(null)
  }

  function undo(id) {
    const next = { ...processed }
    delete next[id]
    setProcessed(next)
    saveStatus(next)
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">기업 인증</h1>
        <p className="text-sm text-gray-500 mt-1">
          대기 {pending.length}건 · 처리 완료 {done.length}건
        </p>
      </div>

      {/* 대기 중 */}
      {pending.length === 0 && done.length === 0 && (
        <div className="bg-white rounded-2xl border border-offwhite-200 py-16 text-center text-sm text-gray-400">
          <Building2 size={32} className="mx-auto mb-3 text-gray-300" />
          처리할 인증 요청이 없습니다
        </div>
      )}

      {pending.length > 0 && (
        <div className="space-y-3">
          {pending.map(b => (
            <div key={b.id} className="bg-white rounded-2xl border border-offwhite-200 p-5">
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center shrink-0">
                    <Building2 size={22} className="text-blue-500" />
                  </div>
                  <div>
                    <p className="font-bold text-navy">{b.company}</p>
                    <p className="text-sm text-gray-500">대표자: {b.representative}</p>
                    <p className="text-xs text-gray-400 mt-0.5">사업자등록번호: {b.businessNumber}</p>
                    <p className="text-xs text-gray-400">요청일: {b.requestedAt}</p>
                  </div>
                </div>
                <div className="flex gap-2 shrink-0">
                  <button
                    onClick={() => approve(b.id)}
                    className="flex items-center gap-1.5 text-sm font-semibold bg-green-50 text-green-700 border border-green-200 px-3 py-1.5 rounded-xl hover:bg-green-100 transition-colors"
                  >
                    <CheckCircle2 size={15} />승인
                  </button>
                  <button
                    onClick={() => openReject(b)}
                    className="flex items-center gap-1.5 text-sm font-semibold bg-red-50 text-red-700 border border-red-200 px-3 py-1.5 rounded-xl hover:bg-red-100 transition-colors"
                  >
                    <XCircle size={15} />반려
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 처리 완료 */}
      {done.length > 0 && (
        <div>
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">처리 완료</p>
          <div className="space-y-3">
            {done.map(b => {
              const result = processed[b.id]
              const isApproved = result.action === 'approved'
              return (
                <div
                  key={b.id}
                  className={`bg-white rounded-2xl border p-5 ${isApproved ? 'border-green-200 bg-green-50/30' : 'border-red-200 bg-red-50/30'}`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-center gap-4">
                      <div className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 ${isApproved ? 'bg-green-100' : 'bg-red-100'}`}>
                        {isApproved
                          ? <CheckCircle2 size={22} className="text-green-600" />
                          : <XCircle     size={22} className="text-red-500" />
                        }
                      </div>
                      <div>
                        <p className="font-bold text-navy">{b.company}</p>
                        <p className="text-sm text-gray-500">대표자: {b.representative}</p>
                        <p className="text-xs text-gray-400 mt-0.5">사업자등록번호: {b.businessNumber}</p>
                        <p className="text-xs text-gray-400">처리일: {result.processedAt}</p>
                        {!isApproved && result.reason && (
                          <p className="text-xs text-red-500 mt-1 flex items-start gap-1">
                            <AlertTriangle size={11} className="shrink-0 mt-0.5" />
                            반려 사유: {result.reason}
                          </p>
                        )}
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-2 shrink-0">
                      <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${isApproved ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                        {isApproved ? '승인됨' : '반려됨'}
                      </span>
                      <button
                        onClick={() => undo(b.id)}
                        className="text-xs text-gray-400 hover:text-navy underline underline-offset-2 transition-colors"
                      >
                        되돌리기
                      </button>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}

      {/* 반려 사유 입력 모달 */}
      {rejectTarget && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setRejectTarget(null)}
        >
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4 overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
              <h3 className="font-bold text-navy">반려 사유 입력</h3>
              <button onClick={() => setRejectTarget(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="p-5">
              <p className="text-sm text-gray-600 mb-4">
                <strong className="text-navy">{rejectTarget.company}</strong>의 인증 요청을 반려합니다.
              </p>
              <textarea
                autoFocus
                rows={4}
                value={rejectReason}
                onChange={e => { setRejectReason(e.target.value); setReasonError('') }}
                placeholder="반려 사유를 입력해 주세요 (예: 서류 미비, 사업자등록번호 불일치 등)"
                className={`w-full text-sm border rounded-xl px-4 py-3 resize-none focus:outline-none transition-colors ${
                  reasonError ? 'border-red-300 focus:border-red-400' : 'border-offwhite-200 focus:border-navy'
                }`}
              />
              {reasonError && (
                <p className="text-xs text-red-500 mt-1 flex items-center gap-1">
                  <AlertTriangle size={11} />{reasonError}
                </p>
              )}
            </div>
            <div className="px-5 pb-5 flex gap-2">
              <button
                onClick={() => setRejectTarget(null)}
                className="flex-1 py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:bg-offwhite-100 transition-colors"
              >
                취소
              </button>
              <button
                onClick={confirmReject}
                className="flex-1 py-2.5 text-sm font-semibold text-white bg-red-500 hover:bg-red-600 rounded-xl transition-colors"
              >
                반려 처리
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
