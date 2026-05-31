    var API_BASE = '';

    var _skipHashChange = false;

    function setHash(page) {
      _skipHashChange = true;
      location.hash = '#/' + page;
      setTimeout(function() { _skipHashChange = false; }, 50);
    }

    function getHashPage() {
      var hash = location.hash.replace('#/', '').replace('#', '');
      return hash || 'home';
    }

    function api(endpoint, options) {
      options = options || {};
      var token = sessionStorage.getItem('token') || localStorage.getItem('token');
      var headers = options.headers || {};
      if (token) {
        headers['Authorization'] = 'Bearer ' + token;
      }
      if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
      }
      return fetch(API_BASE + endpoint, {
        method: options.method || 'GET',
        headers: headers,
        body: options.body
      }).then(function(res) {
        if (res.status === 401) {
          sessionStorage.removeItem('token');
          localStorage.removeItem('token');
          location.reload();
          return Promise.reject(new Error('Unauthorized'));
        }
        return res.json();
      });
    }

    var tabBar, pageTitle, pageSubtitle, contentArea;
    var dataMenu, systemMenu;

    function authHeader() {
      var token = sessionStorage.getItem('token') || localStorage.getItem('token');
      return token ? { 'Authorization': 'Bearer ' + token } : {};
    }

    var pageMap = {
      home: {
        title: '首页概览',
        subtitle: '拍摄任务、照片资产与系统状态总览',
        icon: 'home'
      },
      sample: {
        title: '样品资料',
        subtitle: '管理样品信息、拍摄记录与资料归档',
        icon: 'package'
      },
      gallery: {
        title: '择样图库',
        subtitle: '管理择样图片资料、代号、客户与拍摄记录',
        icon: 'image'
      },
      manufacturer: {
        title: '厂商资料',
        subtitle: '维护合作厂商资料与业务信息',
        icon: 'store'
      },
      friends: {
        title: '好友列表',
        subtitle: '好友会话、群聊、消息与文件沟通',
        icon: 'message-circle'
      },
      users: {
        title: '用户管理',
        subtitle: '管理系统用户、账号状态与角色分配',
        icon: 'users-round'
      },
      roles: {
        title: '角色管理',
        subtitle: '管理系统角色、数据范围与功能权限',
        icon: 'shield-check'
      },
      logs: {
        title: '系统日志',
        subtitle: '查看系统登录日志、操作日志与异常记录',
        icon: 'file-clock'
      }
    };

    var pendingRender = null;

    function refreshIcons() {
      requestAnimationFrame(function() {
        lucide.createIcons({ nodes: [contentArea] });
      });
    }

    function bindToggle(parentId, subId, chevronId) {
      var parent = document.getElementById(parentId);
      var sub = document.getElementById(subId);
      var chevron = document.getElementById(chevronId);

      if (!parent || !sub || !chevron) return;

      var open = false;

      parent.addEventListener('click', function() {
        open = !open;

        if (open) {
          sub.classList.add('open');
          chevron.style.transform = 'rotate(180deg)';
        } else {
          sub.classList.remove('open');
          chevron.style.transform = 'rotate(0deg)';
        }
      });

      return {
        open: function() {
          open = true;
          sub.classList.add('open');
          chevron.style.transform = 'rotate(180deg)';
        }
      };
    }

    function setSidebarActive(page) {
      document.querySelectorAll('.sidebar-item').forEach(function(item) {
        item.classList.remove('active');
      });

      var activePage = isGalleryDetailPage(page) ? 'gallery' : page;
      var navItem = document.querySelector('.sidebar-item[data-page="' + activePage + '"]');

      if (navItem) {
        navItem.classList.add('active');
      }
    }

    function setTabActive(page) {
      document.querySelectorAll('.tab-item').forEach(function(tab) {
        tab.classList.remove('active');
      });

      var tab = document.querySelector('.tab-item[data-page="' + page + '"]');

      if (tab) {
        tab.classList.add('active');
        requestAnimationFrame(function() {
          tab.scrollIntoView({
            behavior: 'smooth',
            inline: 'nearest',
            block: 'nearest'
          });
        });
      }
    }

    function updateHeader(page) {
      if (isGalleryDetailPage(page)) {
        var detail = galleryDetailStore[page] || {};
        var code = detail.code || decodeURIComponent(page.replace('gallery-detail-', ''));
        var customer = detail.customer || '未知客户';
        var photographer = detail.photographer || '未知拍摄人';

        pageTitle.textContent = '择样详情';
        pageSubtitle.textContent = '代号 ' + code + ' · ' + customer + ' · ' + photographer + ' · 图片资料与导出状态';
        return;
      }

      var info = pageMap[page] || {
        title: page,
        subtitle: '当前功能页面',
        icon: 'file-text'
      };

      pageTitle.textContent = info.title;
      pageSubtitle.textContent = info.subtitle;
    }

    function renderContent(page) {
      if (page === 'home') {
        contentArea.innerHTML = getHomeHTML();
        refreshIcons();
        return;
      }

      if (page === 'sample') {
        contentArea.innerHTML = getSampleHTML();
        refreshIcons();
        initSamplePage();
        return;
      }

      if (page === 'gallery') {
        contentArea.innerHTML = getGalleryHTML();
        refreshIcons();
        initGalleryPage();
        return;
      }

      if (page === 'friends') {
        contentArea.innerHTML = getFriendsHTML();
        refreshIcons();
        initFriendsPage();
        return;
      }

      if (page === 'roles') {
        contentArea.innerHTML = getRolesHTML();
        refreshIcons();
        initRolesPage();
        return;
      }

      if (page === 'users') {
        contentArea.innerHTML = getUsersHTML();
        refreshIcons();
        initUsersPage();
        return;
      }

      if (page === 'logs') {
        contentArea.innerHTML = getLogsHTML();
        refreshIcons();
        initLogsPage();
        return;
      }

      if (isGalleryDetailPage(page)) {
        var detail = galleryDetailStore[page] || {};
        var code = detail.code || decodeURIComponent(page.replace('gallery-detail-', ''));
        var customer = detail.customer || '未知客户';
        var photographer = detail.photographer || '未知拍摄人';

        contentArea.innerHTML = getGalleryDetailHTML(code, customer, photographer);
        refreshIcons();
        initGalleryDetailPage(page);
        renderGalleryDetailImages();
        return;
      }

      var info = pageMap[page] || {
        title: page,
        subtitle: '当前功能页面',
        icon: 'file-text'
      };

      contentArea.innerHTML =
        '<div class="empty-page">' +
          '<div>' +
            '<div class="empty-icon">' +
              '<i data-lucide="' + info.icon + '" class="w-8 h-8"></i>' +
            '</div>' +
            '<h3>' + info.title + '</h3>' +
            '<p>' + info.subtitle + '</p>' +
          '</div>' +
        '</div>';

      refreshIcons();
    }

    function switchTo(page) {
      cancelAnimationFrame(pendingRender);
      setHash(page);
      updateHeader(page);
      setTabActive(page);
      setSidebarActive(page);
      pendingRender = requestAnimationFrame(function() {
        pendingRender = requestAnimationFrame(function() {
          renderContent(page);
          pendingRender = null;
        });
      });
    }

    function openTab(page, title, icon) {
      var info = pageMap[page] || {};
      title = title || info.title || page;
      icon = icon || info.icon || 'file-text';

      var existing = document.querySelector('.tab-item[data-page="' + page + '"]');

      if (existing) {
        switchTo(page);
        return;
      }

      var tab = document.createElement('div');
      tab.className = 'tab-item';
      tab.dataset.page = page;
      tab.dataset.title = title;
      tab.dataset.icon = icon;

      tab.innerHTML =
        '<i data-lucide="' + icon + '" class="w-3.5 h-3.5"></i>' +
        '<span>' + title + '</span>' +
        '<span class="tab-close" title="关闭">' +
          '<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.8" stroke-linecap="round">' +
            '<line x1="18" y1="6" x2="6" y2="18"></line>' +
            '<line x1="6" y1="6" x2="18" y2="18"></line>' +
          '</svg>' +
        '</span>';

      tab.addEventListener('click', function(e) {
        if (e.target.closest('.tab-close')) return;
        switchTo(page);
      });

      tab.querySelector('.tab-close').addEventListener('click', function(e) {
        e.stopPropagation();
        closeTab(tab);
      });

      tabBar.appendChild(tab);

      requestAnimationFrame(function() {
        lucide.createIcons({
          nodes: [tab]
        });
      });

      switchTo(page);
    }

    function closeTab(tabEl) {
      var page = tabEl.dataset.page;

      if (page === 'home') return;

      var wasActive = tabEl.classList.contains('active');
      var prev = tabEl.previousElementSibling;
      var next = tabEl.nextElementSibling;

      tabEl.remove();

      if (wasActive) {
        var target = next || prev || document.getElementById('tab-home');

        if (target) {
          switchTo(target.dataset.page);
        }
      }
    }

    function getHomeHTML() {
      return `
        <div class="dashboard">
          <div class="welcome-card">
            <div class="welcome-title">
              <h3>欢迎回来，Admin</h3>
              <p>
                这里是拍照管理系统的工作台。你可以快速查看样品资料、图库资产、厂商信息以及近期拍摄流程。
              </p>
            </div>

            <button class="welcome-action" onclick="openTab('sample', '样品资料', 'package')">
              管理样品
            </button>
          </div>

          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">
                <i data-lucide="database" class="w-[19px] h-[19px]"></i>
              </div>
              <div class="stat-value">128</div>
              <div class="stat-label">样品资料</div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">
                <i data-lucide="image" class="w-[19px] h-[19px]"></i>
              </div>
              <div class="stat-value">3.6k</div>
              <div class="stat-label">图库照片</div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">
                <i data-lucide="store" class="w-[19px] h-[19px]"></i>
              </div>
              <div class="stat-value">56</div>
              <div class="stat-label">合作厂商</div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">
                <i data-lucide="activity" class="w-[19px] h-[19px]"></i>
              </div>
              <div class="stat-value">98%</div>
              <div class="stat-label">系统状态</div>
            </div>
          </div>


        </div>
      `;
    }

    function initApp() {
      tabBar = document.getElementById('tab-bar');
      pageTitle = document.getElementById('page-title');
      pageSubtitle = document.getElementById('page-subtitle');
      contentArea = document.getElementById('content-area');

      dataMenu = bindToggle('nav-data-parent', 'sub-data', 'chevron-data');
      systemMenu = bindToggle('nav-system-parent', 'sub-system', 'chevron-system');

      if (dataMenu) {
        dataMenu.open();
      }

      document.querySelectorAll('.sidebar-item[data-page]').forEach(function(item) {
        item.addEventListener('click', function(e) {
          e.stopPropagation();

          openTab(
            this.dataset.page,
            this.dataset.title,
            this.dataset.icon
          );
        });
      });

      document.getElementById('tab-home').addEventListener('click', function() {
        switchTo('home');
      });

      window.addEventListener('hashchange', function() {
        if (_skipHashChange) return;
        var page = getHashPage();
        var existingTab = document.querySelector('.tab-item[data-page="' + page + '"]');
        if (!existingTab && page !== 'home') {
          var info = pageMap[page] || {};
          openTab(page, info.title, info.icon);
        } else {
          cancelAnimationFrame(pendingRender);
          updateHeader(page);
          setTabActive(page);
          setSidebarActive(page);
          pendingRender = requestAnimationFrame(function() {
            pendingRender = requestAnimationFrame(function() {
              renderContent(page);
              pendingRender = null;
            });
          });
        }
      });

      var initialPage = getHashPage();
      if (initialPage !== 'home') {
        openTab(initialPage);
      } else {
        setHash('home');
        renderContent('home');
      }
    }
