function openModal(id) {
  var el = document.getElementById(id);
  if (el) el.classList.add('show');
  lucide.createIcons();
}

function closeModal(id) {
  var el = document.getElementById(id);
  if (el) el.classList.remove('show');
}

function bindModalClose(closeAttr, modalMaskClass) {
  document.querySelectorAll('[' + closeAttr + ']').forEach(function(btn) {
    btn.addEventListener('click', function() {
      closeModal(this.dataset[closeAttr.replace('data-', '').replace(/-/g, '')]);
    });
  });
}
