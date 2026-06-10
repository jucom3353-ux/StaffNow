@echo off
echo === Promoter 배포 시작 ===

git pull origin backend-0513

echo === 이미지 빌드 중 ===
docker build -t promoter-backend:latest .

echo === 기존 컨테이너 종료 ===
docker-compose -f docker-compose.prod.yml down

echo === 새 컨테이너 실행 ===
docker-compose -f docker-compose.prod.yml up -d

echo === 배포 완료 ===
docker ps