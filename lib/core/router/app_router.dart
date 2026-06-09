import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../features/auth/login_screen.dart';
import '../../features/individual/individual_home_screen.dart';
import '../../features/company/company_home_screen.dart';
import '../../features/admin/admin_home_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/login',
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/individual/home',
        builder: (context, state) => const IndividualHomeScreen(),
      ),
      GoRoute(
        path: '/company/home',
        builder: (context, state) => const CompanyHomeScreen(),
      ),
      GoRoute(
        path: '/admin/home',
        builder: (context, state) => const AdminHomeScreen(),
      ),
    ],
  );
});