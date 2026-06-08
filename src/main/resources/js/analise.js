/* ============================================================
   LicitApp — analise.js
   Responsabilidades:
     1. Renderizar PDFs nos dois painéis com PDF.js (texto selecionável)
     2. Capturar seleção de texto e ancorá-la ao formulário de comentário
     3. Carregar comentários existentes da API (GET)
     4. Enviar novos comentários para a API (POST)
   ============================================================ */

'use strict';

/* ── Configuração do worker do PDF.js ──────────────────────── */
pdfjsLib.GlobalWorkerOptions.workerSrc =
  'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';

/* ── Leitura dos dados injetados pelo Thymeleaf ─────────────── */
const appData = document.getElementById('app-data').dataset;

const API = {
  minutaPdfUrl:         appData.minutaPdfUrl,
  regularizacaoPdfUrl:  appData.regularizacaoPdfUrl,
  comentariosUrl:       appData.comentariosUrl,
};

/* ── Estado da aplicação ─────────────────────────────────────── */
const state = {
  trecho:         null,   // Texto do trecho selecionado no PDF
  scaleLeft:      1.4,    // Zoom do painel esquerdo
  scaleRight:     1.4,    // Zoom do painel direito
  pdfLeft:        null,   // Objeto PDF.js do painel esquerdo
  pdfRight:       null,   // Objeto PDF.js do painel direito
};

/* ── Referências DOM ─────────────────────────────────────────── */
const $ = id => document.getElementById(id);

const viewerLeft       = $('viewer-left');
const viewerRight      = $('viewer-right');
const loadingLeft      = $('loading-left');
const loadingRight     = $('loading-right');
const commentList      = $('comment-list');
const commentEmpty     = $('comment-empty');
const commentCount     = $('comment-count');
const commentTextarea  = $('comment-textarea');
const trechoPreview    = $('trecho-preview');
const trechoText       = $('trecho-text');
const btnSalvar        = $('btn-salvar');
const selectionTooltip = $('selection-tooltip');

/* ============================================================
   1. RENDERIZAÇÃO DO PDF
   ============================================================ */

/**
 * Inicializa um viewer PDF em um container.
 * Renderiza todas as páginas com canvas + text layer selecionável.
 *
 * @param {string}      pdfUrl    - URL do endpoint que retorna o PDF
 * @param {HTMLElement} container - Elemento onde as páginas serão inseridas
 * @param {HTMLElement} loading   - Spinner de carregamento
 * @param {string}      side      - 'left' | 'right' (para controle de zoom)
 * @returns {Promise<PDFDocumentProxy>}
 */
async function initPdfViewer(pdfUrl, container, loading, side) {
  try {
    const loadingTask = pdfjsLib.getDocument(pdfUrl);
    const pdf = await loadingTask.promise;

    if (side === 'left')  state.pdfLeft  = pdf;
    if (side === 'right') state.pdfRight = pdf;

    loading.style.display = 'none';
    await renderAllPages(pdf, container, getScale(side));

    return pdf;

  } catch (err) {
    loading.innerHTML = `
      <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"
           fill="none" stroke="currentColor" stroke-width="1.25"
           style="color: rgba(255,255,255,0.4);" aria-hidden="true">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <span style="font-size:12px;">Erro ao carregar documento.</span>
      <span style="font-size:10px; color: rgba(255,255,255,0.4);">${err.message || 'Verifique a conexão.'}</span>
    `;
    console.error('[LicitApp] Erro no PDF viewer:', err);
  }
}

/**
 * Renderiza todas as páginas de um PDF no container.
 * Cada página tem: canvas (visível) + textLayer (invisível, para seleção de texto).
 */
async function renderAllPages(pdf, container, scale) {
  /* Remove páginas antigas (para re-render ao mudar zoom) */
  container.querySelectorAll('.pdf-page-wrapper').forEach(el => el.remove());

  for (let pageNum = 1; pageNum <= pdf.numPages; pageNum++) {
    const page     = await pdf.getPage(pageNum);
    const viewport = page.getViewport({ scale });

    /* Wrapper da página */
    const pageWrapper = document.createElement('div');
    pageWrapper.className = 'pdf-page-wrapper';

    /* Canvas (imagem do PDF) */
    const canvasWrap = document.createElement('div');
    canvasWrap.className = 'pdf-canvas-wrap';

    const canvas  = document.createElement('canvas');
    const context = canvas.getContext('2d');
    canvas.height = viewport.height;
    canvas.width  = viewport.width;
    canvas.style.width  = '100%';
    canvas.style.height = 'auto';

    canvasWrap.appendChild(canvas);

    /* Text layer (sobreposto, permite selecionar texto) */
    const textLayerDiv = document.createElement('div');
    textLayerDiv.className = 'textLayer';
    textLayerDiv.style.width  = viewport.width  + 'px';
    textLayerDiv.style.height = viewport.height + 'px';
    canvasWrap.appendChild(textLayerDiv);
    canvasWrap.style.position = 'relative';

    /* Número da página */
    const pageNumDiv = document.createElement('div');
    pageNumDiv.className = 'page-num';
    pageNumDiv.textContent = `Página ${pageNum} de ${pdf.numPages}`;

    pageWrapper.appendChild(canvasWrap);
    pageWrapper.appendChild(pageNumDiv);
    container.appendChild(pageWrapper);

    /* Renderiza o canvas */
    await page.render({ canvasContext: context, viewport }).promise;

    /* Renderiza o text layer para permitir seleção */
    const textContent = await page.getTextContent();
    pdfjsLib.renderTextLayer({
      textContent,
      container:  textLayerDiv,
      viewport,
      textDivs: [],
    });
  }
}

/* ── Zoom ─────────────────────────────────────────────────── */

function getScale(side) {
  return side === 'left' ? state.scaleLeft : state.scaleRight;
}

async function zoomIn(side) {
  if (side === 'left') {
    state.scaleLeft = Math.min(state.scaleLeft + 0.2, 3.0);
    if (state.pdfLeft)  await renderAllPages(state.pdfLeft,  viewerLeft,  state.scaleLeft);
  } else {
    state.scaleRight = Math.min(state.scaleRight + 0.2, 3.0);
    if (state.pdfRight) await renderAllPages(state.pdfRight, viewerRight, state.scaleRight);
  }
}

async function zoomOut(side) {
  if (side === 'left') {
    state.scaleLeft = Math.max(state.scaleLeft - 0.2, 0.6);
    if (state.pdfLeft)  await renderAllPages(state.pdfLeft,  viewerLeft,  state.scaleLeft);
  } else {
    state.scaleRight = Math.max(state.scaleRight - 0.2, 0.6);
    if (state.pdfRight) await renderAllPages(state.pdfRight, viewerRight, state.scaleRight);
  }
}

/* ── Download do documento original ──────────────────────── */
function downloadDoc(docId) {
  window.open(`/api/documentos/${docId}/original`, '_blank');
}

/* ============================================================
   2. SELEÇÃO DE TEXTO → ANCORAGEM AO COMENTÁRIO
   ============================================================ */

/*
  Fluxo:
  1. Usuário seleciona texto em qualquer viewer.
  2. Um tooltip flutuante aparece: "Anotar seleção".
  3. Usuário clica no tooltip → o trecho vai para o formulário de comentário.
  4. Usuário escreve e salva.
*/

document.addEventListener('mouseup', handleTextSelection);
document.addEventListener('touchend', handleTextSelection);

function handleTextSelection(event) {
  const selection = window.getSelection();
  const text = selection ? selection.toString().trim() : '';

  if (text.length < 5) {
    hideTooltip();
    return;
  }

  /* Verifica se a seleção está dentro de um dos viewers */
  const inViewer =
    viewerLeft.contains(selection.anchorNode) ||
    viewerRight.contains(selection.anchorNode);

  if (!inViewer) {
    hideTooltip();
    return;
  }

  /* Posiciona o tooltip acima do cursor */
  const x = event.clientX || (event.changedTouches && event.changedTouches[0].clientX) || 0;
  const y = event.clientY || (event.changedTouches && event.changedTouches[0].clientY) || 0;

  showTooltip(x, y, text);
}

function showTooltip(x, y, text) {
  selectionTooltip.classList.add('visible');
  selectionTooltip.style.left = `${x - selectionTooltip.offsetWidth / 2}px`;
  selectionTooltip.style.top  = `${y - 44}px`;

  /* Ao clicar no tooltip, ancora o trecho ao formulário */
  selectionTooltip.onclick = () => {
    anchorTrecho(text);
    hideTooltip();
    window.getSelection()?.removeAllRanges();
  };
}

function hideTooltip() {
  selectionTooltip.classList.remove('visible');
  selectionTooltip.onclick = null;
}

/**
 * Coloca o trecho selecionado na area de preview do formulário.
 */
function anchorTrecho(text) {
  state.trecho = text;

  /* Trunca para exibição (o texto completo fica no state) */
  trechoText.textContent = text.length > 120 ? text.substring(0, 117) + '...' : text;
  trechoPreview.classList.add('visible');

  /* Foca o textarea para o usuário começar a escrever */
  commentTextarea.focus();
}

function clearTrecho() {
  state.trecho = null;
  trechoPreview.classList.remove('visible');
  trechoText.textContent = '';
}

/* Fechar tooltip se o usuário clicar fora */
document.addEventListener('mousedown', e => {
  if (!selectionTooltip.contains(e.target)) {
    hideTooltip();
  }
});

/* ============================================================
   3. CARREGAR COMENTÁRIOS (GET)
   ============================================================ */

async function loadComments() {
  try {
    const resp = await fetch(API.comentariosUrl, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

    const comentarios = await resp.json();
    renderComments(comentarios);

  } catch (err) {
    console.error('[LicitApp] Erro ao carregar comentários:', err);
  }
}

/**
 * Renderiza a lista de comentários na sidebar.
 * @param {Array<Comentario>} comentarios
 */
function renderComments(comentarios) {
  /* Remove itens antigos (mantém o empty state) */
  commentList.querySelectorAll('.comment-item').forEach(el => el.remove());

  if (comentarios.length === 0) {
    commentEmpty.style.display = '';
    commentCount.textContent = '0';
    return;
  }

  commentEmpty.style.display = 'none';
  commentCount.textContent = comentarios.length;

  comentarios.forEach(c => {
    commentList.appendChild(buildCommentEl(c));
  });

  /* Rola para o final */
  commentList.scrollTop = commentList.scrollHeight;
}

/**
 * Constrói o elemento HTML de um comentário.
 * @param {Comentario} c - { id, autorNome, autorIniciais, trecho, texto, criadoEm, documento }
 */
function buildCommentEl(c) {
  const item = document.createElement('div');
  item.className = 'comment-item';
  item.dataset.id = c.id;

  const dateFmt = formatDateTime(c.criadoEm);
  const docTag  = c.documento === 'REGULARIZACAO'
    ? '<span style="font-size:9px;color:var(--status-aprovado-text);background:var(--status-aprovado-bg);padding:1px 6px;border-radius:4px;margin-left:4px;">Reg.</span>'
    : '';

  item.innerHTML = `
    <div class="comment-meta">
      <div class="comment-avatar" aria-hidden="true">${escHtml(c.autorIniciais || 'UC')}</div>
      <span class="comment-author">${escHtml(c.autorNome)}${docTag}</span>
      <span class="comment-time">${dateFmt}</span>
    </div>
    ${c.trecho ? `<div class="comment-trecho" title="${escHtml(c.trecho)}">${escHtml(c.trecho)}</div>` : ''}
    <div class="comment-text">${escHtml(c.texto)}</div>
  `;

  return item;
}

/* ============================================================
   4. ENVIAR COMENTÁRIO (POST)
   ============================================================ */

async function submitComment() {
  const texto = commentTextarea.value.trim();

  if (!texto) {
    commentTextarea.focus();
    commentTextarea.style.borderColor = 'var(--status-pendente-border)';
    setTimeout(() => { commentTextarea.style.borderColor = ''; }, 1500);
    return;
  }

  /* Monta o payload conforme API_CONTRACT.md */
  const payload = {
    texto,
    trecho:    state.trecho || null,
    documento: document.getElementById('doc-selector').value,
  };

  btnSalvar.disabled = true;
  btnSalvar.textContent = 'Salvando...';

  try {
    const resp = await fetch(API.comentariosUrl, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify(payload),
    });

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

    const novo = await resp.json();

    /* Atualiza UI imediatamente (optimistic update) */
    const el = buildCommentEl(novo);
    el.classList.add('new');
    commentEmpty.style.display = 'none';
    commentList.appendChild(el);
    commentList.scrollTop = commentList.scrollHeight;
    commentCount.textContent = commentList.querySelectorAll('.comment-item').length;

    /* Limpa o formulário */
    commentTextarea.value = '';
    clearTrecho();

  } catch (err) {
    console.error('[LicitApp] Erro ao salvar comentário:', err);
    alert('Não foi possível salvar o comentário. Tente novamente.');
  } finally {
    btnSalvar.disabled = false;
    btnSalvar.innerHTML = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true" style="width:13px;height:13px;"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
      Salvar
    `;
  }
}

/* ── Atalho de teclado: Ctrl+Enter salva o comentário ─────── */
commentTextarea.addEventListener('keydown', e => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault();
    submitComment();
  }
});

/* ============================================================
   UTILITÁRIOS
   ============================================================ */

/**
 * Escapa HTML para evitar XSS ao inserir dados do servidor no DOM.
 */
function escHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/**
 * Formata uma data ISO 8601 para o padrão "dd/MM HH:mm".
 */
function formatDateTime(iso) {
  if (!iso) return '';
  try {
    const d = new Date(iso);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const HH = String(d.getHours()).padStart(2, '0');
    const MM = String(d.getMinutes()).padStart(2, '0');
    return `${dd}/${mm} ${HH}:${MM}`;
  } catch { return ''; }
}

/* ============================================================
   INICIALIZAÇÃO
   ============================================================ */

(async function init() {
  /* Roda em paralelo: carrega os dois PDFs e os comentários */
  await Promise.all([
    initPdfViewer(API.minutaPdfUrl,        viewerLeft,  loadingLeft,  'left'),
    initPdfViewer(API.regularizacaoPdfUrl, viewerRight, loadingRight, 'right'),
    loadComments(),
  ]);
})();
