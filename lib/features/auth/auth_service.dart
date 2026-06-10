import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dio/dio.dart';
import 'package:kakao_flutter_sdk_user/kakao_flutter_sdk_user.dart';
import '../../core/network/dio_client.dart';
import '../../core/storage/secure_storage.dart';

final authServiceProvider = Provider<AuthService>((ref) => AuthService());

class AuthService {
  final Dio _dio = DioClient.getInstance();

  // 이메일/비밀번호 로그인
  Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      final response = await _dio.post(
        '/auth/login',
        data: {'email': email, 'password': password},
      );
      final data = response.data['data'] as Map<String, dynamic>;
      return data;
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? '로그인에 실패했습니다';
      throw Exception(message);
    }
  }

  // 카카오 로그인
  Future<Map<String, dynamic>> loginWithKakao() async {
    try {
      // 카카오 SDK로 로그인 → authCode 발급
      OAuthToken token;
      if (await isKakaoTalkInstalled()) {
        token = await UserApi.instance.loginWithKakaoTalk();
      } else {
        token = await UserApi.instance.loginWithKakaoAccount();
      }

      // authCode 백엔드에 전달
      final response = await _dio.post(
        '/auth/kakao/app',
        data: {'authCode': token.accessToken},
      );
      final data = response.data['data'] as Map<String, dynamic>;
      return data;
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? '카카오 로그인에 실패했습니다';
      throw Exception(message);
    } catch (e) {
      throw Exception('카카오 로그인에 실패했습니다');
    }
  }

  // 로그아웃
  Future<void> logout() async {
    try {
      final refreshToken = await SecureStorage.getRefreshToken();
      await _dio.post(
        '/auth/logout',
        data: {'refreshToken': refreshToken},
      );
      // 카카오 로그아웃
      await UserApi.instance.logout();
    } catch (_) {
    } finally {
      await SecureStorage.clear();
    }
  }

  // 토큰 갱신
  Future<Map<String, dynamic>> refreshToken() async {
    try {
      final refreshToken = await SecureStorage.getRefreshToken();
      final response = await _dio.post(
        '/auth/refresh',
        data: {'refreshToken': refreshToken},
      );
      return response.data['data'] as Map<String, dynamic>;
    } on DioException catch (e) {
      final message = e.response?.data?['message'] ?? '토큰 재발급에 실패했습니다';
      throw Exception(message);
    }
  }
}