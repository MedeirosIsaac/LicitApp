/* ============================================================
   LicitApp — nav.js
   Sistema de transições suaves entre páginas.
   Incluir em todos os arquivos de preview e templates.
   ============================================================ */

'use strict';

(function () {

  /* ── Cria o overlay de transição ── */
  const overlay = document.createElement('div');
  overlay.className = 'page-transition-overlay';
  document.body.appendChild(overlay);

  /* ── Destaca o item ativo na sidebar com base na URL atual ── */
  function setActiveNav() {
    const current = window.location.pathname.split('/').pop();
    document.querySelectorAll('.nav-item, .folder-item').forEach(el => {
      const href = el.getAttribute('href') || '';
      el.classList.toggle('active', href !== '#' && href !== '' && current.includes(href.split('/').pop().replace('preview-', '').replace('.html', '')));
    });
  }

  /* ── Intercepta todos os links internos para fazer a transição ── */
  function initTransitions() {
    document.querySelectorAll('a[href]').forEach(link => {
      const href = link.getAttribute('href');

      /* Ignora links externos, âncoras e javascript: */
      if (!href || href.startsWith('http') || href.startsWith('#') || href.startsWith('javascript')) return;

      link.addEventListener('click', function (e) {
        e.preventDefault();
        navigateTo(href);
      });
    });
  }

  /**
   * Navega para uma URL com transição de fade.
   * @param {string} url
   */
  function navigateTo(url) {
    /* Fade out */
    overlay.classList.add('fade-out');

    setTimeout(() => {
      window.location.href = url;
    }, 200); /* tempo igual à duração do CSS transition */
  }

  /* ── Fade de entrada (limpa o overlay ao chegar na nova página) ── */
  window.addEventListener('pageshow', () => {
    overlay.style.transition = 'none';
    overlay.classList.remove('fade-out');
    /* Reativa a transição após um frame para a próxima saída */
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        overlay.style.transition = '';
      });
    });
  });

  /* ── Expõe navigateTo globalmente (para botões com onclick) ── */
  window.licitNav = { navigateTo };

  /* ── Inicializa ao carregar ── */
  document.addEventListener('DOMContentLoaded', () => {
    setActiveNav();
    initTransitions();
  });

})();
