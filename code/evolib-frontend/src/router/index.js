import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { auth: false } },
  { path: '/reader/search', name: 'BookSearch', component: () => import('@/views/reader/BookSearchView.vue'), meta: { role: 'ROLE_READER' } },
  { path: '/reader/detail/:isbn', name: 'BookDetail', component: () => import('@/views/reader/BookDetailView.vue'), meta: { role: 'ROLE_READER' } },
  { path: '/circulation/borrow', name: 'Borrow', component: () => import('@/views/circulation/BorrowView.vue'), meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/return', name: 'Return', component: () => import('@/views/circulation/ReturnView.vue'), meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/register', name: 'Register', component: () => import('@/views/circulation/RegisterReaderView.vue'), meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/borrows', name: 'Borrows', component: () => import('@/views/circulation/ReaderBorrowsView.vue'), meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/admin/add-book', name: 'AddBook', component: () => import('@/views/admin/AddBookView.vue'), meta: { role: 'ROLE_ADMIN' } },
  { path: '/admin/remove-book', name: 'RemoveBook', component: () => import('@/views/admin/RemoveBookView.vue'), meta: { role: 'ROLE_ADMIN' } },
  { path: '/admin/reset-password', name: 'ResetPassword', component: () => import('@/views/admin/ResetPasswordView.vue'), meta: { role: 'ROLE_ADMIN' } },
  { path: '/:pathMatch(.*)*', redirect: '/login' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  if (to.meta.auth === false) return next();
  if (!auth.isLoggedIn) return next('/login');
  if (to.meta.role && !auth.hasRole(to.meta.role)) {
    return next(auth.homePath);
  }
  next();
});

export default router;