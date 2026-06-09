import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'auth_service.dart';
import '../../core/storage/secure_storage.dart';
import '../../core/fcm/fcm_service.dart';

final authControllerProvider =
StateNotifierProvider<AuthController, AsyncValue<void>>(
      (ref) => AuthController(ref.read(authServiceProvider)),
);

class AuthController extends StateNotifier<AsyncValue<void>> {
  final AuthService _authService;

  AuthController(this._authService) : super(const AsyncValue.data(null));

  Future<void> login({
    required String email,
    required String password,
    required BuildContext context,
  }) async {
    state = const AsyncValue.loading();
    try {
      final data = await _authService.login(email, password);

      final accessToken = data['accessToken'] as String;
      final refreshToken = data['refreshToken'] as String;
      final user = data['user'] as Map<String, dynamic>;
      final role = user['role'] as String;

      await SecureStorage.saveTokens(
        accessToken: accessToken,
        refreshToken: refreshToken,
        role: role,
      );

      // FCM 초기화 (로그인 성공 후)
      await FcmService.init();

      state = const AsyncValue.data(null);

      if (!context.mounted) return;

      switch (role) {
        case 'INDIVIDUAL':
          context.go('/individual/home');
        case 'COMPANY':
        case 'MANAGER':
          context.go('/company/home');
        case 'ADMIN':
          context.go('/admin/home');
        default:
          context.go('/login');
      }
    } catch (e) {
      state = AsyncValue.error(e, StackTrace.current);
      if (!context.mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Future<void> logout(BuildContext context) async {
    // FCM 토큰 삭제
    await FcmService.removeToken();
    await _authService.logout();
    if (!context.mounted) return;
    context.go('/login');
  }
}