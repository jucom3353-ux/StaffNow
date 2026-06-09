import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dio/dio.dart';
import '../../core/network/dio_client.dart';
import '../../core/storage/secure_storage.dart';

final authServiceProvider = Provider<AuthService>((ref) => AuthService());

class AuthService {
  final Dio _dio = DioClient.getInstance();

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

  Future<void> logout() async {
    try {
      final refreshToken = await SecureStorage.getRefreshToken();
      await _dio.post(
        '/auth/logout',
        data: {'refreshToken': refreshToken},
      );
    } catch (_) {
    } finally {
      await SecureStorage.clear();
    }
  }

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