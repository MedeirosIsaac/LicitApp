'use strict';

pdfjsLib.GlobalWorkerOptions.workerSrc =
  'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';

const appData = document.getElementById('app-data').dataset;

const API = {
  minutaPdfUrl:        appData.minutaPdfUrl        || '/pdfs/minuta.pdf',
  regularizacaoPdfUrl: appData.regularizacaoPdfUrl || '/pdfs/aditivo.pdf',
  comentariosUrl:      appData.comentariosUrl,
};

const state = {
  trecho:      null,
  scaleLeft:   1.4,
  scaleRight:  1.4,
  pdfLeft:     null,
  pdfRight:    null,
};

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

// ── PDF Viewer ────────────────────────────────────────────────────────────────

async function initPdfViewer(pdfUrl, container, loading, side) {
  try {
    const pdf = await pdfjsLib.getDocument(pdfUrl).promise;

    // Guarda referência ANTES de renderizar — resolve o bug do zoom
    if (side === 'left')  state.pdfLeft  = pdf;
    if (side === 'right') state.pdfRight = pdf;

    loading.style.display = 'none';
    await renderAllPages(pdf, container, getScale(side));
    return pdf;
  } catch (err) {
    loading.innerHTML = `
      <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24"
           fill="none" stroke="currentColor" stroke-width="1.25"
           style="color:rgba(255,255,255,0.4)">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <span style="font-size:12px;">Erro ao carregar documento.</span>
      <span style="font-size:10px;color:rgba(255,255,255,0.4);">${err.message || 'Verifique a URL do PDF.'}</span>
    `;
    console.error('[LicitApp] Erro PDF viewer:', err);
  }
}

async function renderAllPages(pdf, container, scale) {
  container.querySelectorAll('.pdf-page-wrapper').forEach(el => el.remove());

  for (let pageNum = 1; pageNum <= pdf.numPages; pageNum++) {
    const page     = await pdf.getPage(pageNum);
    const viewport = page.getViewport({ scale });

    const pageWrapper = document.createElement('div');
    pageWrapper.className = 'pdf-page-wrapper';

    const canvasWrap = document.createElement('div');
    canvasWrap.className = 'pdf-canvas-wrap';
    canvasWrap.style.position = 'relative';
    // Necessário para o PDF.js renderizar a textLayer corretamente
    canvasWrap.style.setProperty('--scale-factor', scale);

    const canvas  = document.createElement('canvas');
    const context = canvas.getContext('2d');
    canvas.height = viewport.height;
    canvas.width  = viewport.width;
    canvas.style.width  = '100%';
    canvas.style.height = 'auto';
    canvasWrap.appendChild(canvas);

    const textLayerDiv = document.createElement('div');
    textLayerDiv.className = 'textLayer';
    textLayerDiv.style.width  = viewport.width  + 'px';
    textLayerDiv.style.height = viewport.height + 'px';
    canvasWrap.appendChild(textLayerDiv);

    const pageNumDiv = document.createElement('div');
    pageNumDiv.className = 'page-num';
    pageNumDiv.textContent = `Página ${pageNum} de ${pdf.numPages}`;

    pageWrapper.appendChild(canvasWrap);
    pageWrapper.appendChild(pageNumDiv);
    container.appendChild(pageWrapper);

    await page.render({ canvasContext: context, viewport }).promise;

    const textContent = await page.getTextContent();
    pdfjsLib.renderTextLayer({
      textContentSource: textContent,
      container: textLayerDiv,
      viewport,
      textDivs: [],
    });
  }
}

function getScale(side) {
  return side === 'left' ? state.scaleLeft : state.scaleRight;
}

// ── Zoom ─────────────────────────────────────────────────────────────────────

async function zoomIn(side) {
  if (side === 'left') {
    state.scaleLeft = Math.min(state.scaleLeft + 0.2, 3.0);
    if (state.pdfLeft) await renderAllPages(state.pdfLeft, viewerLeft, state.scaleLeft);
  } else {
    state.scaleRight = Math.min(state.scaleRight + 0.2, 3.0);
    if (state.pdfRight) await renderAllPages(state.pdfRight, viewerRight, state.scaleRight);
  }
}

async function zoomOut(side) {
  if (side === 'left') {
    state.scaleLeft = Math.max(state.scaleLeft - 0.2, 0.6);
    if (state.pdfLeft) await renderAllPages(state.pdfLeft, viewerLeft, state.scaleLeft);
  } else {
    state.scaleRight = Math.max(state.scaleRight - 0.2, 0.6);
    if (state.pdfRight) await renderAllPages(state.pdfRight, viewerRight, state.scaleRight);
  }
}

// ── Download — baixa o PDF que está sendo exibido ────────────────────────────

function downloadDoc(side) {
  const url = side === 'left' ? API.minutaPdfUrl : API.regularizacaoPdfUrl;
  const a = document.createElement('a');
  a.href = url;
  a.download = side === 'left' ? 'minuta.pdf' : 'regularizacao.pdf';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}

// ── Seleção de texto ──────────────────────────────────────────────────────────

document.addEventListener('mouseup',  handleTextSelection);
document.addEventListener('touchend', handleTextSelection);

function handleTextSelection(event) {
  const selection = window.getSelection();
  const text = selection ? selection.toString().trim() : '';
  if (text.length < 5) { hideTooltip(); return; }

  const inViewer =
    viewerLeft.contains(selection.anchorNode) ||
    viewerRight.contains(selection.anchorNode);
  if (!inViewer) { hideTooltip(); return; }

  const x = event.clientX || (event.changedTouches && event.changedTouches[0].clientX) || 0;
  const y = event.clientY || (event.changedTouches && event.changedTouches[0].clientY) || 0;
  showTooltip(x, y, text);
}

function showTooltip(x, y, text) {
  selectionTooltip.classList.add('visible');
  selectionTooltip.style.left = `${x - selectionTooltip.offsetWidth / 2}px`;
  selectionTooltip.style.top  = `${y - 44}px`;
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

function anchorTrecho(text) {
  state.trecho = text;
  trechoText.textContent = text.length > 120 ? text.substring(0, 117) + '...' : text;
  trechoPreview.classList.add('visible');
  commentTextarea.focus();
}

function clearTrecho() {
  state.trecho = null;
  trechoPreview.classList.remove('visible');
  trechoText.textContent = '';
}

document.addEventListener('mousedown', e => {
  if (!selectionTooltip.contains(e.target)) hideTooltip();
});

// ── Comentários ───────────────────────────────────────────────────────────────

async function loadComments() {
  if (!API.comentariosUrl) return;
  try {
    const resp = await fetch(API.comentariosUrl, { headers: { 'Accept': 'application/json' } });
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    renderComments(await resp.json());
  } catch (err) {
    console.warn('[LicitApp] Comentários não carregados:', err.message);
  }
}

function renderComments(comentarios) {
  commentList.querySelectorAll('.comment-item').forEach(el => el.remove());
  if (!comentarios || comentarios.length === 0) {
    commentEmpty.style.display = '';
    commentCount.textContent = '0';
    return;
  }
  commentEmpty.style.display = 'none';
  commentCount.textContent = comentarios.length;
  comentarios.forEach(c => commentList.appendChild(buildCommentEl(c)));
  commentList.scrollTop = commentList.scrollHeight;
}

function buildCommentEl(c) {
  const item = document.createElement('div');
  item.className = 'comment-item';
  item.dataset.id = c.id || '';

  const dateFmt  = formatDateTime(c.dataCriacao || c.criadoEm);
  const iniciais = c.autorIniciais || (c.autor ? c.autor.substring(0, 2).toUpperCase() : 'UC');
  const docTag   = (c.documento === 'REGULARIZACAO')
    ? '<span style="font-size:9px;color:var(--status-aprovado-text);background:var(--status-aprovado-bg);padding:1px 6px;border-radius:4px;margin-left:4px;">Reg.</span>'
    : '';

  item.innerHTML = `
    <div class="comment-meta">
      <div class="comment-avatar">${escHtml(iniciais)}</div>
      <span class="comment-author">${escHtml(c.autor || c.autorNome || 'Analista')}${docTag}</span>
      <span class="comment-time">${dateFmt}</span>
    </div>
    ${c.trecho ? `<div class="comment-trecho" title="${escHtml(c.trecho)}">${escHtml(c.trecho)}</div>` : ''}
    <div class="comment-text">${escHtml(c.texto)}</div>
  `;
  return item;
}

async function submitComment() {
  const texto = commentTextarea.value.trim();
  if (!texto) {
    commentTextarea.focus();
    commentTextarea.style.borderColor = 'var(--status-pendente-border)';
    setTimeout(() => { commentTextarea.style.borderColor = ''; }, 1500);
    return;
  }

  const payload = {
    texto,
    trecho:    state.trecho || null,
    autor:     'Isaac Costa',
    documento: document.getElementById('doc-selector').value,
  };

  btnSalvar.disabled = true;
  btnSalvar.textContent = 'Salvando...';

  try {
    if (API.comentariosUrl) {
      const resp = await fetch(API.comentariosUrl, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify(payload),
      });
      if (resp.ok) {
        appendComment(await resp.json());
        commentTextarea.value = '';
        clearTrecho();
        return;
      }
    }

    // Fallback local
    appendComment({
      id:          Date.now().toString(),
      texto:       payload.texto,
      trecho:      payload.trecho,
      autor:       payload.autor,
      documento:   payload.documento,
      dataCriacao: new Date().toISOString(),
    });
    commentTextarea.value = '';
    clearTrecho();

  } catch (err) {
    console.warn('[LicitApp] Fallback local:', err.message);
    appendComment({
      id:          Date.now().toString(),
      texto:       payload.texto,
      trecho:      payload.trecho,
      autor:       payload.autor,
      documento:   payload.documento,
      dataCriacao: new Date().toISOString(),
    });
    commentTextarea.value = '';
    clearTrecho();
  } finally {
    btnSalvar.disabled = false;
    btnSalvar.innerHTML = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
           stroke="currentColor" stroke-width="2" style="width:13px;height:13px;">
        <line x1="22" y1="2" x2="11" y2="13"/>
        <polygon points="22 2 15 22 11 13 2 9 22 2"/>
      </svg>
      Salvar
    `;
  }
}

function appendComment(c) {
  const el = buildCommentEl(c);
  el.classList.add('new');
  commentEmpty.style.display = 'none';
  commentList.appendChild(el);
  commentList.scrollTop = commentList.scrollHeight;
  commentCount.textContent = commentList.querySelectorAll('.comment-item').length;
}

commentTextarea.addEventListener('keydown', e => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault();
    submitComment();
  }
});

// ── Utilitários ───────────────────────────────────────────────────────────────

function escHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function formatDateTime(iso) {
  if (!iso) return '';
  try {
    const d  = new Date(iso);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const HH = String(d.getHours()).padStart(2, '0');
    const MM = String(d.getMinutes()).padStart(2, '0');
    return `${dd}/${mm} ${HH}:${MM}`;
  } catch { return ''; }
}

// ── Init — sequencial para garantir que state.pdfLeft/Right sejam preenchidos
//    antes de qualquer clique de zoom ──────────────────────────────────────────

(async function init() {
  await initPdfViewer(API.minutaPdfUrl,        viewerLeft,  loadingLeft,  'left');
  await initPdfViewer(API.regularizacaoPdfUrl, viewerRight, loadingRight, 'right');
  loadComments();
})();