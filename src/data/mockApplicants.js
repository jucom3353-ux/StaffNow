// 35명 지원자 더미 데이터 — shift-demo 시연용
// Tier 1 (고수): rating 4.5–5.0, hireCount 15+  → 8명
// Tier 2 (실력자): rating 3.8–4.4, hireCount 5–14 → 12명
// Tier 3 (보통): rating 2.8–3.7, hireCount 1–8   → 8명
// Tier 4 (신규): rating null, hireCount 0–1       → 7명

export const MOCK_APPLICANTS = [
  // ── Tier 1: 고수 ──────────────────────────────────────
  { id: 'ap-01', name: '김서연', age: 28, gender: '여', region: '서울 강남구', rating: 4.9, hireCount: 31, joinedAt: '2023-03' },
  { id: 'ap-02', name: '박준호', age: 32, gender: '남', region: '서울 서초구', rating: 4.8, hireCount: 27, joinedAt: '2023-01' },
  { id: 'ap-03', name: '이지은', age: 25, gender: '여', region: '서울 송파구', rating: 4.8, hireCount: 19, joinedAt: '2023-06' },
  { id: 'ap-04', name: '최현우', age: 30, gender: '남', region: '서울 강남구', rating: 4.7, hireCount: 24, joinedAt: '2023-02' },
  { id: 'ap-05', name: '정다영', age: 27, gender: '여', region: '서울 마포구', rating: 4.7, hireCount: 18, joinedAt: '2023-05' },
  { id: 'ap-06', name: '황민석', age: 35, gender: '남', region: '경기 성남시', rating: 4.6, hireCount: 22, joinedAt: '2022-11' },
  { id: 'ap-07', name: '강소희', age: 24, gender: '여', region: '서울 강동구', rating: 4.6, hireCount: 15, joinedAt: '2023-08' },
  { id: 'ap-08', name: '유재원', age: 29, gender: '남', region: '서울 서초구', rating: 4.5, hireCount: 20, joinedAt: '2023-04' },

  // ── Tier 2: 실력자 ─────────────────────────────────────
  { id: 'ap-09', name: '임수진', age: 26, gender: '여', region: '서울 영등포구', rating: 4.4, hireCount: 12, joinedAt: '2023-07' },
  { id: 'ap-10', name: '손태민', age: 31, gender: '남', region: '서울 용산구',   rating: 4.3, hireCount: 10, joinedAt: '2023-09' },
  { id: 'ap-11', name: '한지수', age: 23, gender: '여', region: '경기 수원시',   rating: 4.3, hireCount:  8, joinedAt: '2024-01' },
  { id: 'ap-12', name: '오승현', age: 28, gender: '남', region: '서울 중구',     rating: 4.2, hireCount: 14, joinedAt: '2023-10' },
  { id: 'ap-13', name: '노아영', age: 24, gender: '여', region: '서울 성동구',   rating: 4.2, hireCount:  9, joinedAt: '2024-02' },
  { id: 'ap-14', name: '배태훈', age: 33, gender: '남', region: '서울 송파구',   rating: 4.1, hireCount: 11, joinedAt: '2023-11' },
  { id: 'ap-15', name: '전지민', age: 27, gender: '여', region: '서울 강서구',   rating: 4.1, hireCount:  7, joinedAt: '2024-03' },
  { id: 'ap-16', name: '고민준', age: 29, gender: '남', region: '서울 노원구',   rating: 4.0, hireCount:  6, joinedAt: '2024-01' },
  { id: 'ap-17', name: '문채원', age: 25, gender: '여', region: '경기 고양시',   rating: 4.0, hireCount:  8, joinedAt: '2023-12' },
  { id: 'ap-18', name: '안재현', age: 30, gender: '남', region: '서울 은평구',   rating: 3.9, hireCount:  5, joinedAt: '2024-02' },
  { id: 'ap-19', name: '신유리', age: 22, gender: '여', region: '서울 동대문구', rating: 3.9, hireCount:  4, joinedAt: '2024-04' },
  { id: 'ap-20', name: '장원영', age: 26, gender: '여', region: '서울 강남구',   rating: 3.8, hireCount:  9, joinedAt: '2023-08' },

  // ── Tier 3: 보통 ───────────────────────────────────────
  { id: 'ap-21', name: '임동현', age: 34, gender: '남', region: '경기 부천시',   rating: 3.7, hireCount:  3, joinedAt: '2024-03' },
  { id: 'ap-22', name: '권나은', age: 21, gender: '여', region: '서울 관악구',   rating: 3.5, hireCount:  2, joinedAt: '2024-05' },
  { id: 'ap-23', name: '홍석민', age: 38, gender: '남', region: '서울 강북구',   rating: 3.4, hireCount:  6, joinedAt: '2023-07' },
  { id: 'ap-24', name: '윤서영', age: 23, gender: '여', region: '경기 안양시',   rating: 3.3, hireCount:  4, joinedAt: '2024-04' },
  { id: 'ap-25', name: '남기훈', age: 36, gender: '남', region: '서울 도봉구',   rating: 3.2, hireCount:  1, joinedAt: '2024-05' },
  { id: 'ap-26', name: '조미래', age: 28, gender: '여', region: '서울 중랑구',   rating: 3.1, hireCount:  3, joinedAt: '2024-03' },
  { id: 'ap-27', name: '백승준', age: 31, gender: '남', region: '경기 용인시',   rating: 3.0, hireCount:  2, joinedAt: '2024-06' },
  { id: 'ap-28', name: '류지현', age: 24, gender: '여', region: '서울 금천구',   rating: 2.8, hireCount:  1, joinedAt: '2024-07' },

  // ── Tier 4: 신규 ───────────────────────────────────────
  { id: 'ap-29', name: '이준서', age: 20, gender: '남', region: '서울 강남구',   rating: null, hireCount: 0, joinedAt: '2026-04' },
  { id: 'ap-30', name: '김하은', age: 22, gender: '여', region: '서울 서초구',   rating: null, hireCount: 0, joinedAt: '2026-03' },
  { id: 'ap-31', name: '박도현', age: 21, gender: '남', region: '경기 성남시',   rating: null, hireCount: 1, joinedAt: '2026-02' },
  { id: 'ap-32', name: '최아린', age: 19, gender: '여', region: '서울 송파구',   rating: null, hireCount: 0, joinedAt: '2026-05' },
  { id: 'ap-33', name: '정재원', age: 23, gender: '남', region: '서울 강남구',   rating: null, hireCount: 0, joinedAt: '2026-04' },
  { id: 'ap-34', name: '오서현', age: 20, gender: '여', region: '서울 마포구',   rating: null, hireCount: 1, joinedAt: '2026-01' },
  { id: 'ap-35', name: '강현우', age: 22, gender: '남', region: '경기 수원시',   rating: null, hireCount: 0, joinedAt: '2026-05' },
]
