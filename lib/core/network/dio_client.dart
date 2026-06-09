import 'package:dio/dio.dart';
import '../storage/secure_storage.dart';

class DioClient {
  static const baseUrl = 'http://10.0.2.2:8080'; // 에뮬레이터용 로컬 백엔드

  static Dio getInstance() {
    final dio = Dio(BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {'X-Client-Type': 'APP'},
    ));

    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await SecureStorage.getAccessToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
      onError: (error, handler) async {
        if (error.response?.statusCode == 401) {
          await SecureStorage.clear();
          // TODO: 로그인 화면으로 리다이렉트
        }
        return handler.next(error);
      },
    ));

    return dio;
  }
}