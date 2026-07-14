import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { auth: false } },
  { path: '/reader/search', name: 'BookSearch', component: () => import('@/views/reader/BookSearchView.vue') },
  { path: '/reader/detail/:isbn', name: 'BookDetail', component: () => import('@/views/reader/BookDetailView.vue') },
  { path: '/circulation/borrow', name: 'Borrow', component: () => import('@/views/circulation/BorrowView.vue') },
  { path: '/circulation/borrow/form', name: 'BorrowForm', component: () => import('@/views/circulation/BorrowFormView.vue') },
  { path: '/circulation/return', name: 'Return', component: () => import('@/views/circulation/ReturnView.vue') },
  { path: '/circulation/register', name: 'Register', component: () => import('@/views/circulation/RegisterReaderView.vue') },
  { path: '/circulation/borrows', name: 'Borrows', component: () => import('@/views/circulation/ReaderBorrowsView.vue') },
  { path: '/admin/add-book', name: 'AddBook', component: () => import('@/views/admin/AddBookView.vue') },
  { path: '/admin/remove-book', name: 'RemoveBook', component: () => import('@/views/admin/RemoveBookView.vue') },
  { path: '/admin/reset-password', name: 'ResetPassword', component: () => import('@/views/admin/ResetPasswordView.vue') },
  { path: '/orgtag/list', name: 'orgtagList', component: () => import('@/views/orgtag/OrgTagList.vue') },
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
  next();
});

export default router;