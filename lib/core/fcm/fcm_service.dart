import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:dio/dio.dart';
import '../network/dio_client.dart';
import '../storage/secure_storage.dart';

class FcmService {
  static final FirebaseMessaging _messaging = FirebaseMessaging.instance;
  static final Dio _dio = DioClient.getInstance();

  static Future<void> init() async {
    // 권한 요청
    await _messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );

    // 토큰 발급 후 백엔드 저장
    final token = await _messaging.getToken();
    if (token != null) {
      await _registerToken(token);
    }

    // 토큰 갱신 시 자동 재등록
    _messaging.onTokenRefresh.listen((newToken) {
      _registerToken(newToken);
    });

    // 포그라운드 메시지 수신
    FirebaseMessaging.onMessage.listen((RemoteMessage message) {
      // TODO: 혁준이 팝업 UI 구현 시 여기서 처리
    });
  }

  static Future<void> _registerToken(String token) async {
    try {
      final accessToken = await SecureStorage.getAccessToken();
      if (accessToken == null) return;

      await _dio.post(
        '/fcm/token',
        data: {
          'token': token,
          'deviceInfo': 'Android',
        },
      );
    } catch (e) {
      // 토큰 등록 실패해도 앱 동작에 영향 없음
    }
  }

  static Future<void> removeToken() async {
    try {
      final token = await _messaging.getToken();
      if (token == null) return;
      await _dio.delete('/fcm/token', data: {'token': token});
      await _messaging.deleteToken();
    } catch (_) {}
  }
}