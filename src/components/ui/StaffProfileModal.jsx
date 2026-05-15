import { X, MapPin, Briefcase, Clock, User } from 'lucide-react'

const AVATAR_COLORS = [
  'bg-navy', 'bg-orange', 'bg-emerald-500', 'bg-violet-500',
  'bg-sky-500', 'bg-rose-500', 'bg-amber-500', 'bg-teal-500',
]

const MBTI_DESCRIPTIONS = {
  INTJ: '전략가형 — 독립적이고 결단력 있는 분석가',
  INTP: '논리학자형 — 창의적이고 탐구적인 사색가',
  ENTJ: '통솔자형 — 대담하고 상상력 풍부한 리더',
  ENTP: '변론가형 — 영리하고 호기심 많은 사상가',
  INFJ: '옹호자형 — 조용하지만 영감을 주는 이상주의자',
  INFP: '중재자형 — 시적이고 친절하며 이타적인 사람',
  ENFJ: '선도자형 — 카리스마 있고 영감을 주는 지도자',
  ENFP: '활동가형 — 열정적이고 창의적이며 사교적',
  ISTJ: '현실주의자형 — 사실에 충실한 신뢰할 수 있는 사람',
  ISFJ: '수호자형 — 헌신적이고 따뜻한 보호자',
  ESTJ: '경영자형 — 탁월한 관리자, 행정의 달인',
  ESFJ: '집정관형 — 배려심 깊고 사교적인 팀플레이어',
  ISTP: '장인형 — 대담하고 실용적인 실험가',
  ISFP: '모험가형 — 유연하고 매력적인 예술가',
  ESTP: '기업가형 — 영리하고 에너지 넘치는 행동파',
  ESFP: '연예인형 — 즉흥적이고 활발한 엔터테이너',
}

function getNowLabel(score) {
  if (score === null || score === undefined) return null
  if (score >= 40) return { text: '에이스', color: 'text-orange bg-orange/10' }
  if (score >= 25) return { text: '우수', color: 'text-green-600 bg-green-50' }
  if (score >= 15) return { text: '일반', color: 'text-blue-600 bg-blue-50' }
  if (score >= 8)  return { text: '주의', color: 'text-amber-600 bg-amber-50' }
  return { text: '관리', color: 'text-red-500 bg-red-50' }
}

function Avatar({ name, colorIndex }) {
  let photoUrl = null
  try { photoUrl = localStorage.getItem(`staffnow_avatar_${name}`) || null } catch {}
  const bg = AVATAR_COLORS[(colorIndex ?? 0) % AVATAR_COLORS.length]
  if (photoUrl) {
    return (
      <div className="w-16 h-16 rounded-full overflow-hidden shrink-0">
        <img src={photoUrl} alt="" className="w-full h-full object-cover" />
      </div>
    )
  }
  return (
    <div className={`w-16 h-16 rounded-full ${bg} flex items-center justify-center shrink-0`}>
      <span className="text-white text-2xl font-bold">{name?.[0] ?? '?'}</span>
    </div>
  )
}

function StatChip({ label, value, accent }) {
  return (
    <div className={`flex flex-col items-center px-4 py-2.5 rounded-xl ${accent}`}>
      <span className="text-lg font-bold leading-tight">{value}</span>
      <span className="text-xs mt-0.5 opacity-70">{label}</span>
    </div>
  )
}

export default function StaffProfileModal({ person, colorIndex = 0, onClose }) {
  if (!person) return null

  const nowLabel = getNowLabel(person.nowScore)
  const mbtiDesc = person.mbti ? MBTI_DESCRIPTIONS[person.mbti] : null
  const hasDetail = person.skills?.length || person.mbti || person.preferredTimes || person.bio

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/40 px-0 sm:px-4"
      onClick={onClose}
    >
      <div
        className="bg-white w-full sm:max-w-md rounded-t-3xl sm:rounded-2xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col"
        onClick={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between px-5 pt-5 pb-3 shrink-0">
          <span className="text-sm font-semibold text-gray-400">인력 프로필</span>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-gray-400 hover:bg-offwhite hover:text-gray-600 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* 스크롤 바디 */}
        <div className="overflow-y-auto px-5 pb-6 space-y-5 flex-1">

          {/* 기본 정보 */}
          <div className="flex items-center gap-4">
            <Avatar name={person.name} colorIndex={colorIndex} />
            <div className="flex-1 min-w-0">
              <p className="text-xl font-bold text-navy">{person.name}</p>
              <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5 mt-0.5">
                {person.age && (
                  <span className="text-sm text-gray-500 flex items-center gap-1">
                    <User size={12} />{person.age}세
                  </span>
                )}
                {person.gender && (
                  <span className="text-sm text-gray-500">{person.gender}</span>
                )}
                {person.region && (
                  <span className="text-sm text-gray-500 flex items-center gap-1">
                    <MapPin size={12} />{person.region}
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* 통계 칩 */}
          <div className="flex gap-2">
            <StatChip
              label="근무 완료"
              value={`${person.hireCount ?? 0}회`}
              accent="bg-navy/5 text-navy flex-1"
            />
            {person.rating != null ? (
              <StatChip
                label="평점"
                value={`★ ${Number(person.rating).toFixed(1)}`}
                accent="bg-yellow-50 text-yellow-700 flex-1"
              />
            ) : (
              <StatChip
                label="평점"
                value="신규"
                accent="bg-gray-50 text-gray-400 flex-1"
              />
            )}
            {person.nowScore != null && (
              <StatChip
                label="NOW 지수"
                value={`${person.nowScore}`}
                accent={`${nowLabel?.color ?? 'bg-gray-50 text-gray-500'} flex-1`}
              />
            )}
          </div>

          {/* NOW 지수 등급 뱃지 */}
          {nowLabel && (
            <div className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-semibold ${nowLabel.color}`}>
              <span>NOW {person.nowScore}</span>
              <span className="opacity-60">·</span>
              <span>{nowLabel.text}</span>
            </div>
          )}

          {!hasDetail && (
            <div className="py-6 text-center text-sm text-gray-400">
              백엔드 연동 후 상세 정보가 표시됩니다
            </div>
          )}

          {/* MBTI */}
          {person.mbti && (
            <div className="space-y-1.5">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">MBTI</p>
              <div className="flex items-start gap-3 bg-offwhite rounded-xl p-3">
                <span className="text-sm font-bold text-navy shrink-0">{person.mbti}</span>
                {mbtiDesc && <p className="text-xs text-gray-500 leading-snug">{mbtiDesc}</p>}
              </div>
            </div>
          )}

          {/* 보유 스킬 */}
          {person.skills?.length > 0 && (
            <div className="space-y-1.5">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">보유 스킬</p>
              <div className="flex flex-wrap gap-1.5">
                {person.skills.map(s => (
                  <span key={s} className="px-2.5 py-1 text-xs font-medium bg-navy/5 text-navy rounded-full">
                    {s}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* 선호 근무 시간 */}
          {person.preferredTimes && (
            <div className="space-y-1.5">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide flex items-center gap-1">
                <Clock size={11} />선호 근무
              </p>
              <p className="text-sm text-gray-600">{person.preferredTimes}</p>
            </div>
          )}

          {/* 자기소개 */}
          {person.bio && (
            <div className="space-y-1.5">
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide flex items-center gap-1">
                <Briefcase size={11} />자기소개
              </p>
              <p className="text-sm text-gray-600 leading-relaxed">{person.bio}</p>
            </div>
          )}
        </div>

        {/* 푸터 */}
        <div className="px-5 py-4 border-t border-offwhite-200 shrink-0">
          <button
            onClick={onClose}
            className="w-full py-3 rounded-xl bg-offwhite text-navy text-sm font-semibold hover:bg-offwhite-200 transition-colors"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  )
}
