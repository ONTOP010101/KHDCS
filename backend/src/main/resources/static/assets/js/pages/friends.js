    function getFriendsHTML() {
      return `
    <div class="friends-page">
      <!-- 左侧好友列表 -->
      <div class="friends-card friends-left">
        <div class="friends-left-head">
          <div class="friends-title-row">
            <div class="friends-title">
              <div class="friends-title-icon">
                <i data-lucide="message-circle"></i>
              </div>
              <div>
                <strong>消息</strong>
                <span>好友 / 群聊</span>
              </div>
            </div>

            <div class="friends-head-actions">
              <button class="friends-btn friends-btn-purple" id="friend-btn-create-group" title="创建群聊">
                <i data-lucide="users-round"></i>
              </button>

              <button class="friends-btn friends-btn-primary" id="friend-btn-add" title="添加好友">
                <i data-lucide="user-plus"></i>
              </button>
            </div>
          </div>

          <div class="friends-search">
            <i data-lucide="search"></i>
            <input type="search" id="friend-search-input-main" placeholder="搜索好友、角色或部门..." />
          </div>
        </div>

        <div class="friends-tabs">
          <button class="friends-tab active" data-status="all">全部</button>
          <button class="friends-tab" data-status="online">在线</button>
          <button class="friends-tab" data-status="away">离开</button>
          <button class="friends-tab" data-status="offline">离线</button>
        </div>

        <div class="friends-list" id="friends-list"></div>
      </div>

      <!-- 中间聊天区 -->
      <div class="friends-card friends-chat">
        <div class="friend-chat-header">
          <div class="friend-avatar" id="chat-current-avatar"></div>

          <div class="friend-chat-title">
            <strong id="chat-current-name">请选择好友</strong>
            <span id="chat-current-subtitle">从左侧选择一个好友开始沟通</span>
          </div>

          <div class="friend-chat-actions">
            <button class="friend-icon-btn" id="friend-btn-profile" title="好友资料">
              <i data-lucide="user"></i>
            </button>
            <button class="friend-icon-btn" id="friend-btn-call" title="语音通话">
              <i data-lucide="phone"></i>
            </button>
            <button class="friend-icon-btn" id="friend-btn-video" title="视频通话">
              <i data-lucide="video"></i>
            </button>
            <button class="friend-icon-btn" id="friend-btn-more" title="更多操作">
              <i data-lucide="more-horizontal"></i>
            </button>
          </div>
        </div>

        <div class="friend-chat-messages" id="friend-chat-messages"></div>

        <div class="friend-typing" id="friend-typing">
          对方正在输入...
        </div>

        <div class="friend-input-area">
          <div class="friend-input-row">
            <div class="friend-emoji-panel" id="friend-emoji-panel">
              <div class="friend-emoji-grid" id="friend-emoji-grid"></div>
            </div>

            <button class="friend-icon-btn" id="friend-btn-emoji" title="表情">
              <i data-lucide="smile"></i>
            </button>

            <button class="friend-icon-btn" id="friend-btn-attach" title="发送附件">
              <i data-lucide="paperclip"></i>
            </button>

            <button class="friend-icon-btn" id="friend-btn-image" title="发送图片">
              <i data-lucide="image"></i>
            </button>

            <input type="file" id="friend-file-input" style="display:none;" />
            <input type="file" id="friend-image-input" accept="image/*" style="display:none;" />

            <textarea id="friend-message-input" rows="1" placeholder="输入消息，Enter 发送，Shift + Enter 换行..."></textarea>

            <button class="friends-btn friends-btn-primary" id="friend-btn-send" title="发送">
              <i data-lucide="send"></i>
              发送
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧资料区 -->
      <div class="friends-card friends-right">
        <div class="friend-profile-panel">
          <div class="friend-profile-avatar" id="profile-avatar"></div>
          <div class="friend-profile-name" id="profile-name">请选择好友</div>
          <div class="friend-profile-role" id="profile-role">暂无资料</div>
          <div class="friend-profile-badge offline" id="profile-status">未选择</div>
        </div>

        <div class="friend-profile-body">
          <div class="friend-info-row">
            <span>好友 ID</span>
            <strong id="profile-id">-</strong>
          </div>
          <div class="friend-info-row">
            <span>部门</span>
            <strong id="profile-dept">-</strong>
          </div>
          <div class="friend-info-row">
            <span>加入时间</span>
            <strong id="profile-join">-</strong>
          </div>
          <div class="friend-info-row">
            <span>最近在线</span>
            <strong id="profile-last">-</strong>
          </div>

          <div class="friend-stat-grid">
            <div class="friend-stat-card">
              <span>消息数</span>
              <strong id="profile-msg-count">0</strong>
            </div>
            <div class="friend-stat-card">
              <span>文件数</span>
              <strong id="profile-file-count">0</strong>
            </div>
          </div>

          <div class="friend-profile-actions">
            <button class="friends-btn friends-btn-primary" id="profile-action-call">
              <i data-lucide="phone"></i>
              发起通话
            </button>

            <button class="friends-btn friends-btn-ghost" id="profile-action-clear">
              <i data-lucide="eraser"></i>
              清空聊天
            </button>

            <button class="friends-btn friends-btn-ghost" id="profile-action-block">
              <i data-lucide="ban"></i>
              拉黑好友
            </button>

            <button class="friends-btn friends-btn-danger" id="profile-action-delete">
              <i data-lucide="user-minus"></i>
              删除好友
            </button>
          </div>
        </div>
      </div>

      ${getFriendModalsHTML()}

<div class="friend-more-popover" id="friend-more-popover">
  <div class="friend-more-head">
    <div class="friend-more-avatar" id="friend-more-avatar"></div>
    <div class="friend-more-title">
      <strong id="friend-more-name">好友</strong>
      <span>更多操作</span>
    </div>
  </div>

  <div class="friend-more-divider"></div>

  <button class="friend-more-item" id="friend-more-top">
    <i data-lucide="pin"></i>
    <span>置顶当前会话</span>
  </button>

  <button class="friend-more-item" id="friend-more-mute">
    <i data-lucide="bell-off"></i>
    <span id="friend-more-mute-text">设置免打扰</span>
  </button>

  <button class="friend-more-item" id="friend-more-search">
    <i data-lucide="search"></i>
    <span>搜索聊天记录</span>
  </button>

  <button class="friend-more-item" id="friend-more-export">
    <i data-lucide="download"></i>
    <span>导出聊天记录</span>
  </button>

  <div class="friend-more-divider"></div>

  <button class="friend-more-item danger" id="friend-more-clear">
    <i data-lucide="eraser"></i>
    <span>清空聊天记录</span>
  </button>
</div>
    </div>
  `;
    }

    function getFriendModalsHTML() {
      return `
    <!-- 添加好友 -->
    <div class="friend-modal-mask" id="modal-add-friend">
      <div class="friend-modal">
        <div class="friend-modal-head">
          <strong>添加好友</strong>
          <button class="friend-icon-btn" data-close-modal="modal-add-friend">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="friend-modal-body">
          <input class="friend-modal-input" id="friend-add-search" placeholder="请输入用户名、邮箱或手机号..." />
          <div class="friend-result-list" id="friend-add-results"></div>
        </div>

        <div class="friend-modal-foot">
          <button class="friends-btn friends-btn-ghost" data-close-modal="modal-add-friend">取消</button>
        </div>
      </div>
    </div>

    <!-- 创建群聊 -->
    <div class="friend-modal-mask" id="modal-create-group">
      <div class="friend-modal">
        <div class="friend-modal-head">
          <strong>创建群聊</strong>
          <button class="friend-icon-btn" data-close-modal="modal-create-group">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="friend-modal-body">
          <div style="margin-bottom:12px;">
            <input class="friend-modal-input" id="friend-group-name" placeholder="请输入群聊名称..." />
          </div>

          <input class="friend-modal-input" id="friend-group-search" placeholder="搜索成员..." />

          <div style="margin-top:10px;color:rgba(29,29,31,0.45);font-size:12px;font-weight:700;">
            已选择 <strong id="friend-group-selected-count">0</strong> 人
          </div>

          <div class="friend-member-list" id="friend-group-members"></div>
        </div>

        <div class="friend-modal-foot">
          <button class="friends-btn friends-btn-ghost" data-close-modal="modal-create-group">取消</button>
          <button class="friends-btn friends-btn-purple" id="friend-confirm-create-group">
            <i data-lucide="check"></i>
            创建群聊
          </button>
        </div>
      </div>
    </div>

    <!-- 好友资料弹窗 -->
    <div class="friend-modal-mask" id="modal-friend-profile">
      <div class="friend-modal">
        <div class="friend-modal-head">
          <strong>好友资料</strong>
          <button class="friend-icon-btn" data-close-modal="modal-friend-profile">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="friend-modal-body" id="friend-profile-modal-body"></div>

        <div class="friend-modal-foot">
          <button class="friends-btn friends-btn-ghost" data-close-modal="modal-friend-profile">关闭</button>
        </div>
      </div>
    </div>
  `;
    }

    var friendState = {
      currentId: 1,
      filterStatus: 'all',
      search: '',
      selectedGroupMembers: new Set()
    };

    var friendListData = [
      {
        id: 1,
        name: 'Alex',
        avatar: 'A',
        color1: '#007aff',
        color2: '#5ac8fa',
        role: '设计师',
        department: '设计部',
        status: 'online',
        statusText: '在线',
        joinDate: '2024-01-15',
        lastOnline: '刚刚',
        time: '09:41',
        unread: 0,
        blocked: false,
        preview: '好的，我马上看一下，有结果回复你',
        files: 2,
        messages: [
          { from: 'other', text: '你好！新样品的数据我整理好了，请查收 📋' },
          { from: 'me', text: '收到，我看一下 👀' },
          { from: 'me', text: '样品数据看起来不错，有几个细节需要确认一下，方便现在沟通吗？' },
          { from: 'other', text: '可以的，你说' },
          { from: 'me', text: 'A001 号样品的尺寸数据好像有点问题，你帮忙复核一下' },
          { from: 'other', text: '好的，我马上看一下，有结果回复你' }
        ]
      },
      {
        id: 2,
        name: 'Bella',
        avatar: 'B',
        color1: '#ff2d55',
        color2: '#ff9f0a',
        role: '摄影师',
        department: '摄影部',
        status: 'online',
        statusText: '在线',
        joinDate: '2024-02-20',
        lastOnline: '刚刚',
        time: '昨天',
        unread: 2,
        blocked: false,
        preview: '好的，明天见～',
        files: 5,
        messages: [
          { from: 'other', text: '图片我已经拍完一批了' },
          { from: 'me', text: '辛苦，明天我核对一下' },
          { from: 'other', text: '好的，明天见～' }
        ]
      },
      {
        id: 3,
        name: 'Chris',
        avatar: 'C',
        color1: '#ff9f0a',
        color2: '#ffcc00',
        role: '运营',
        department: '运营部',
        status: 'away',
        statusText: '离开',
        joinDate: '2024-03-10',
        lastOnline: '15 分钟前',
        time: '周一',
        unread: 0,
        blocked: false,
        preview: '文件已发送',
        files: 8,
        messages: [
          { from: 'other', type: 'file', fileName: '择样数据汇总.xlsx', fileSize: '248 KB' },
          { from: 'other', text: '文件已发送' }
        ]
      },
      {
        id: 4,
        name: 'Diana',
        avatar: 'D',
        color1: '#34c759',
        color2: '#30d158',
        role: '数据管理员',
        department: '数据部',
        status: 'offline',
        statusText: '离线',
        joinDate: '2024-04-05',
        lastOnline: '昨天 18:20',
        time: '2024/03/10',
        unread: 0,
        blocked: false,
        preview: '谢谢！',
        files: 1,
        messages: [
          { from: 'me', text: '数据字段我已经补好了' },
          { from: 'other', text: '谢谢！' }
        ]
      },
      {
        id: 5,
        name: 'Eric',
        avatar: 'E',
        color1: '#ff3b30',
        color2: '#ff6961',
        role: '技术',
        department: '技术部',
        status: 'online',
        statusText: '在线',
        joinDate: '2024-05-18',
        lastOnline: '刚刚',
        time: '14:20',
        unread: 1,
        blocked: false,
        preview: '这个样品确认一下...',
        files: 3,
        messages: [
          { from: 'other', text: '这个样品确认一下...' }
        ]
      },
      {
        id: 6,
        name: 'Fiona',
        avatar: 'F',
        color1: '#af52de',
        color2: '#5856d6',
        role: '产品',
        department: '产品部',
        status: 'away',
        statusText: '离开',
        joinDate: '2024-06-22',
        lastOnline: '30 分钟前',
        time: '昨天',
        unread: 0,
        blocked: false,
        preview: '订单已确认',
        files: 4,
        messages: [
          { from: 'other', text: '订单已确认' }
        ]
      }
    ];

    function initFriendsPage() {
      renderFriendList();
      renderFriendChat();
      renderFriendProfile();
      renderFriendEmojiGrid();
      bindFriendEvents();
    }

    function getCurrentFriend() {
      return friendListData.find(function(f) {
        return f.id === friendState.currentId;
      }) || friendListData[0];
    }

    function renderFriendList() {
      var list = document.getElementById('friends-list');
      if (!list) return;

      var keyword = (friendState.search || '').toLowerCase();

      var data = friendListData.filter(function(f) {
        var matchStatus = friendState.filterStatus === 'all' || f.status === friendState.filterStatus;
        var matchKeyword =
          !keyword ||
          f.name.toLowerCase().indexOf(keyword) > -1 ||
          f.role.toLowerCase().indexOf(keyword) > -1 ||
          f.department.toLowerCase().indexOf(keyword) > -1;

        return matchStatus && matchKeyword;
      });

      if (!data.length) {
        list.innerHTML = `
      <div style="padding:30px;text-align:center;color:rgba(29,29,31,0.42);font-size:13px;font-weight:700;">
        暂无匹配好友
      </div>
    `;
        return;
      }

      list.innerHTML = data.map(function(f) {
        return `
      <div class="friend-item ${f.id === friendState.currentId ? 'active' : ''}" data-id="${f.id}">
        <div class="friend-avatar" style="background:linear-gradient(135deg,${f.color1},${f.color2});">
          ${f.avatar}
          <span class="friend-status-dot ${f.status}"></span>
        </div>

        <div class="friend-main">
          <div class="friend-row">
            <div class="friend-name">${f.name}</div>
            <div class="friend-time">${f.time}</div>
          </div>

          <div class="friend-row">
            <div class="friend-preview">${f.blocked ? '已拉黑' : f.preview}</div>
            ${f.unread > 0 ? '<span class="friend-unread">' + f.unread + '</span>' : ''}
          </div>
        </div>
      </div>
    `;
      }).join('');

      list.querySelectorAll('.friend-item').forEach(function(item) {
        item.addEventListener('click', function() {
          var id = Number(this.dataset.id);
          friendState.currentId = id;

          var f = getCurrentFriend();
          f.unread = 0;

          renderFriendList();
          renderFriendChat();
          renderFriendProfile();
          lucide.createIcons();
        });
      });

      bindClick('friend-more-top', function() {
        closeFriendMorePopover();
        friendSetTop();
      });

      bindClick('friend-more-mute', function() {
        closeFriendMorePopover();
        friendMuteChat();
      });

      bindClick('friend-more-search', function() {
        closeFriendMorePopover();
        openFriendSearchPanel();
      });

      bindClick('friend-more-export', function() {
        closeFriendMorePopover();
        friendExportChat();
      });

      bindClick('friend-more-clear', async function() {
        closeFriendMorePopover();

        var f = getCurrentFriend();

        var ok = await appConfirm('确定清空与「' + f.name + '」的聊天记录吗？清空后当前页面将不再显示这些消息。', {
          title: '清空聊天记录',
          type: 'danger',
          icon: 'eraser'
        });

        if (!ok) return;

        f.messages = [];
        f.preview = '暂无聊天记录';

        renderFriendList();
        renderFriendChat();
        renderFriendProfile();

        appToast('聊天记录已清空');
      });

      document.addEventListener('click', function(e) {
        var popover = document.getElementById('friend-more-popover');
        var moreBtn = document.getElementById('friend-btn-more');

        if (!popover || !popover.classList.contains('show')) return;

        if (popover.contains(e.target)) return;
        if (moreBtn && moreBtn.contains(e.target)) return;

        closeFriendMorePopover();
      });
    }

    function renderFriendChat() {
      var f = getCurrentFriend();
      if (!f) return;

      var avatar = document.getElementById('chat-current-avatar');
      var name = document.getElementById('chat-current-name');
      var subtitle = document.getElementById('chat-current-subtitle');
      var messages = document.getElementById('friend-chat-messages');

      if (avatar) {
        avatar.textContent = f.avatar;
        avatar.style.background = 'linear-gradient(135deg,' + f.color1 + ',' + f.color2 + ')';
      }

      if (name) name.textContent = f.name;
      if (subtitle) {
        subtitle.textContent = f.statusText + ' · ' + f.role + ' · ' + f.department;
      }

      if (!messages) return;

      messages.innerHTML = `
    <div class="friend-date-line"><span>今天</span></div>
    ${f.messages.map(function(m) {
      return renderFriendMessage(m, f);
    }).join('')}
  `;

      messages.scrollTop = messages.scrollHeight;
      lucide.createIcons();
    }

    function renderFriendMessage(m, f) {
      var isMe = m.from === 'me';

      if (m.type === 'file') {
        return `
      <div class="friend-msg ${isMe ? 'me' : 'other'}">
        ${isMe ? '' : `<div class="friend-msg-avatar" style="background:linear-gradient(135deg,${f.color1},${f.color2});">${f.avatar}</div>`}
        <div class="friend-bubble">
          <div class="friend-file-bubble">
            <div class="friend-file-icon">
              <i data-lucide="file-text"></i>
            </div>
            <div class="friend-file-info">
              <strong>${m.fileName}</strong>
              <span>${m.fileSize || '未知大小'} · 点击下载</span>
            </div>
          </div>
        </div>
      </div>
    `;
      }

      if (m.type === 'image') {
        return `
      <div class="friend-msg ${isMe ? 'me' : 'other'}">
        ${isMe ? '' : `<div class="friend-msg-avatar" style="background:linear-gradient(135deg,${f.color1},${f.color2});">${f.avatar}</div>`}
        <div class="friend-bubble">
          <div style="width:180px;height:120px;border-radius:16px;background:linear-gradient(135deg,#007aff88,#5ac8fa66);display:flex;align-items:center;justify-content:center;">
            <i data-lucide="image" style="color:white;width:34px;height:34px;"></i>
          </div>
          <div style="margin-top:6px;font-size:11px;opacity:.75;">${m.fileName || '图片'}</div>
        </div>
      </div>
    `;
      }

      return `
    <div class="friend-msg ${isMe ? 'me' : 'other'}">
      ${isMe ? '' : `<div class="friend-msg-avatar" style="background:linear-gradient(135deg,${f.color1},${f.color2});">${f.avatar}</div>`}
      <div class="friend-bubble">${escapeHTML(m.text)}</div>
    </div>
  `;
    }

    function renderFriendProfile() {
      var f = getCurrentFriend();
      if (!f) return;

      var avatar = document.getElementById('profile-avatar');
      var name = document.getElementById('profile-name');
      var role = document.getElementById('profile-role');
      var status = document.getElementById('profile-status');

      if (avatar) {
        avatar.textContent = f.avatar;
        avatar.style.background = 'linear-gradient(135deg,' + f.color1 + ',' + f.color2 + ')';
      }

      if (name) name.textContent = f.name;
      if (role) role.textContent = f.role + ' · ' + f.department;

      if (status) {
        status.className = 'friend-profile-badge ' + f.status;
        status.textContent = f.blocked ? '已拉黑' : f.statusText;
      }

      setText('profile-id', '#' + String(f.id).padStart(4, '0'));
      setText('profile-dept', f.department);
      setText('profile-join', f.joinDate);
      setText('profile-last', f.lastOnline);
      setText('profile-msg-count', f.messages.length);
      setText('profile-file-count', f.files);
    }

    function setText(id, text) {
      var el = document.getElementById(id);
      if (el) el.textContent = text;
    }

    function escapeHTML(str) {
      return String(str || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
    }

    function bindFriendEvents() {
      var search = document.getElementById('friend-search-input-main');
      if (search) {
        search.addEventListener('input', function() {
          friendState.search = this.value.trim();
          renderFriendList();
        });
      }

      document.querySelectorAll('.friends-tab').forEach(function(btn) {
        btn.addEventListener('click', function() {
          document.querySelectorAll('.friends-tab').forEach(function(b) {
            b.classList.remove('active');
          });

          this.classList.add('active');
          friendState.filterStatus = this.dataset.status;
          renderFriendList();
        });
      });

      bindClick('friend-btn-add', function() {
        openFriendModal('modal-add-friend');
        renderFriendAddResults('');
        var input = document.getElementById('friend-add-search');
        if (input) {
          input.value = '';
          setTimeout(function() { input.focus(); }, 50);
        }
      });

      bindClick('friend-btn-create-group', function() {
        friendState.selectedGroupMembers.clear();
        setText('friend-group-selected-count', '0');
        var groupName = document.getElementById('friend-group-name');
        var groupSearch = document.getElementById('friend-group-search');
        if (groupName) groupName.value = '';
        if (groupSearch) groupSearch.value = '';
        renderGroupMembers('');
        openFriendModal('modal-create-group');
      });

      bindClick('friend-btn-profile', function() {
        openFriendProfileModal();
      });

      bindClick('friend-btn-call', function() {
        var f = getCurrentFriend();
        appAlert('正在向 ' + f.name + ' 发起语音通话...', {
          title: '语音通话',
          type: 'info',
          icon: 'phone'
        });
      });

      bindClick('friend-btn-video', function() {
        var f = getCurrentFriend();
        appAlert('正在向 ' + f.name + ' 发起视频通话...', {
          title: '视频通话',
          type: 'info',
          icon: 'video'
        });
      });

      bindClick('friend-btn-more', function(e) {
        openFriendMorePopover(e.currentTarget);
      });

      bindClick('friend-btn-emoji', function() {
        var panel = document.getElementById('friend-emoji-panel');
        if (panel) panel.classList.toggle('show');
      });

      bindClick('friend-btn-attach', function() {
        var input = document.getElementById('friend-file-input');
        if (input) input.click();
      });

      bindClick('friend-btn-image', function() {
        var input = document.getElementById('friend-image-input');
        if (input) input.click();
      });

      bindClick('friend-btn-send', function() {
        sendFriendMessage();
      });

      var msgInput = document.getElementById('friend-message-input');
      if (msgInput) {
        msgInput.addEventListener('keydown', function(e) {
          if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendFriendMessage();
          }
        });

        msgInput.addEventListener('input', function() {
          this.style.height = 'auto';
          this.style.height = Math.min(this.scrollHeight, 116) + 'px';
        });
      }

      var fileInput = document.getElementById('friend-file-input');
      if (fileInput) {
        fileInput.addEventListener('change', function() {
          if (!this.files || !this.files[0]) return;

          var f = getCurrentFriend();
          f.messages.push({
            from: 'me',
            type: 'file',
            fileName: this.files[0].name,
            fileSize: formatFileSize(this.files[0].size)
          });

          f.files += 1;
          f.preview = '[文件] ' + this.files[0].name;
          f.time = '刚刚';

          this.value = '';
          renderFriendList();
          renderFriendChat();
          renderFriendProfile();
        });
      }

      var imageInput = document.getElementById('friend-image-input');
      if (imageInput) {
        imageInput.addEventListener('change', function() {
          if (!this.files || !this.files[0]) return;

          var f = getCurrentFriend();
          f.messages.push({
            from: 'me',
            type: 'image',
            fileName: this.files[0].name
          });

          f.files += 1;
          f.preview = '[图片] ' + this.files[0].name;
          f.time = '刚刚';

          this.value = '';
          renderFriendList();
          renderFriendChat();
          renderFriendProfile();
        });
      }

      bindClick('profile-action-call', function() {
        var f = getCurrentFriend();
        appAlert('正在向 ' + f.name + ' 发起语音通话...', {
          title: '语音通话',
          type: 'info',
          icon: 'phone'
        });
      });

      bindClick('profile-action-clear', async function() {
        var f = getCurrentFriend();

        var ok = await appConfirm('确定清空与 ' + f.name + ' 的聊天记录吗？清空后当前页面将不再显示这些消息。', {
          title: '清空聊天记录',
          type: 'warning',
          icon: 'eraser'
        });

        if (!ok) return;

        f.messages = [];
        f.preview = '暂无聊天记录';

        renderFriendList();
        renderFriendChat();
        renderFriendProfile();

        appToast('聊天记录已清空');
      });

      bindClick('profile-action-block', function() {
        var f = getCurrentFriend();
        f.blocked = !f.blocked;

        var btn = document.getElementById('profile-action-block');
        if (btn) {
          btn.innerHTML = f.blocked
            ? '<i data-lucide="shield-check"></i> 取消拉黑'
            : '<i data-lucide="ban"></i> 拉黑好友';
        }

        renderFriendList();
        renderFriendProfile();
        lucide.createIcons();
      });

      bindClick('profile-action-delete', async function() {
        var f = getCurrentFriend();

        var ok = await appConfirm('确定删除好友「' + f.name + '」吗？删除后将从好友列表中移除。', {
          title: '删除好友',
          type: 'danger',
          icon: 'user-minus'
        });

        if (!ok) return;

        friendListData = friendListData.filter(function(item) {
          return item.id !== f.id;
        });

        friendState.currentId = friendListData[0] ? friendListData[0].id : null;

        renderFriendList();
        renderFriendChat();
        renderFriendProfile();

        appToast('好友已删除');
      });

      document.querySelectorAll('[data-close-modal]').forEach(function(btn) {
        btn.addEventListener('click', function() {
          closeFriendModal(this.dataset.closeModal);
        });
      });

      document.querySelectorAll('.friend-modal-mask').forEach(function(mask) {
        mask.addEventListener('click', function(e) {
          if (e.target === mask) closeFriendModal(mask.id);
        });
      });

      var addSearch = document.getElementById('friend-add-search');
      if (addSearch) {
        addSearch.addEventListener('input', function() {
          renderFriendAddResults(this.value.trim());
        });
      }

      var groupSearch = document.getElementById('friend-group-search');
      if (groupSearch) {
        groupSearch.addEventListener('input', function() {
          renderGroupMembers(this.value.trim());
        });
      }

      bindClick('friend-confirm-create-group', function() {
        var nameInput = document.getElementById('friend-group-name');
        var name = nameInput ? nameInput.value.trim() : '';

        if (!name) {
          appAlert('请输入群聊名称', {
            title: '提示',
            type: 'warning',
            icon: 'edit-3'
          });
          return;
        }

        if (friendState.selectedGroupMembers.size === 0) {
          appAlert('请选择至少一位成员', {
            title: '提示',
            type: 'warning',
            icon: 'users'
          });
          return;
        }

        closeFriendModal('modal-create-group');
        appAlert('群聊「' + name + '」创建成功，共 ' + friendState.selectedGroupMembers.size + ' 位成员。', {
          title: '创建成功',
          type: 'success',
          icon: 'check-circle'
        });
      });
    }

    function bindClick(id, fn) {
      var el = document.getElementById(id);
      if (el) el.addEventListener('click', fn);
    }

    function formatFileSize(size) {
      if (size < 1024) return size + ' B';
      if (size < 1024 * 1024) return Math.round(size / 1024) + ' KB';
      return (size / 1024 / 1024).toFixed(1) + ' MB';
    }

    function sendFriendMessage() {
      var input = document.getElementById('friend-message-input');
      if (!input) return;

      var text = input.value.trim();
      if (!text) return;

      var f = getCurrentFriend();

      if (f.blocked) {
        appAlert('该好友已被拉黑，无法发送消息', {
          title: '无法发送',
          type: 'warning',
          icon: 'ban'
        });
        return;
      }

      f.messages.push({
        from: 'me',
        text: text
      });

      f.preview = text;
      f.time = '刚刚';

      input.value = '';
      input.style.height = 'auto';

      renderFriendList();
      renderFriendChat();
      renderFriendProfile();

      simulateFriendReply(f);
    }

    function simulateFriendReply(friend) {
      var typing = document.getElementById('friend-typing');
      if (typing) {
        typing.textContent = friend.name + ' 正在输入...';
        typing.classList.add('show');
      }

      setTimeout(function() {
        if (typing) typing.classList.remove('show');

        if (friendState.currentId !== friend.id) {
          friend.unread += 1;
        }

        friend.messages.push({
          from: 'other',
          text: '收到，我稍后确认一下。'
        });

        friend.preview = '收到，我稍后确认一下。';
        friend.time = '刚刚';

        renderFriendList();

        if (friendState.currentId === friend.id) {
          renderFriendChat();
          renderFriendProfile();
        }
      }, 900);
    }

    function renderFriendEmojiGrid() {
      var grid = document.getElementById('friend-emoji-grid');
      if (!grid) return;

      var emojis = [
        '😀','😂','🤣','😍','🥰','😘','😎','🤩','🥳',
        '😊','😉','🙃','😋','🤔','🫡','😢','😭','😡',
        '🎉','❤️','💙','💚','💯','🔥','⭐','👍','👏',
        '🙌','🤝','💪','📷','📋','✅','❌','⚠️','💡',
        '📌','📎','📁','📊','📈','🚀'
      ];

      grid.innerHTML = emojis.map(function(e) {
        return '<button type="button" data-emoji="' + e + '">' + e + '</button>';
      }).join('');

      grid.querySelectorAll('button').forEach(function(btn) {
        btn.addEventListener('click', function() {
          var input = document.getElementById('friend-message-input');
          if (input) {
            input.value += this.dataset.emoji;
            input.focus();
          }

          var panel = document.getElementById('friend-emoji-panel');
          if (panel) panel.classList.remove('show');
        });
      });
    }

    function openFriendModal(id) {
      var modal = document.getElementById(id);
      if (modal) modal.classList.add('show');
    }

    function closeFriendModal(id) {
      var modal = document.getElementById(id);
      if (modal) modal.classList.remove('show');
    }

    function openFriendProfileModal() {
      var f = getCurrentFriend();
      var body = document.getElementById('friend-profile-modal-body');
      if (!body) return;

      body.innerHTML = `
    <div style="text-align:center;">
      <div class="friend-profile-avatar" style="background:linear-gradient(135deg,${f.color1},${f.color2});">${f.avatar}</div>
      <div class="friend-profile-name">${f.name}</div>
      <div class="friend-profile-role">${f.role} · ${f.department}</div>
      <div class="friend-profile-badge ${f.status}">${f.statusText}</div>
    </div>

    <div style="margin-top:18px;">
      <div class="friend-info-row"><span>好友 ID</span><strong>#${String(f.id).padStart(4, '0')}</strong></div>
      <div class="friend-info-row"><span>加入时间</span><strong>${f.joinDate}</strong></div>
      <div class="friend-info-row"><span>最近在线</span><strong>${f.lastOnline}</strong></div>
      <div class="friend-info-row"><span>消息数量</span><strong>${f.messages.length}</strong></div>
      <div class="friend-info-row"><span>文件数量</span><strong>${f.files}</strong></div>
    </div>
  `;

      openFriendModal('modal-friend-profile');
    }

    function renderFriendAddResults(keyword) {
      var box = document.getElementById('friend-add-results');
      if (!box) return;

      var users = [
        { id: 101, name: '张三', email: 'zhangsan@example.com', role: '设计师' },
        { id: 102, name: '李四', email: 'lisi@example.com', role: '摄影师' },
        { id: 103, name: '王五', email: 'wangwu@example.com', role: '数据管理员' },
        { id: 104, name: '赵六', email: 'zhaoliu@example.com', role: '运营' }
      ];

      if (!keyword) {
        box.innerHTML = `
      <div style="padding:20px;text-align:center;color:rgba(29,29,31,0.42);font-size:13px;font-weight:700;">
        输入关键词搜索用户
      </div>
    `;
        return;
      }

      var lower = keyword.toLowerCase();
      var matched = users.filter(function(u) {
        return u.name.toLowerCase().indexOf(lower) > -1 ||
               u.email.toLowerCase().indexOf(lower) > -1 ||
               u.role.toLowerCase().indexOf(lower) > -1;
      });

      if (!matched.length) {
        box.innerHTML = `
      <div style="padding:20px;text-align:center;color:rgba(29,29,31,0.42);font-size:13px;font-weight:700;">
        没有找到匹配用户
      </div>
    `;
        return;
      }

      box.innerHTML = matched.map(function(u) {
        return `
      <div class="friend-result-item">
        <div class="friend-avatar" style="width:42px;height:42px;background:linear-gradient(135deg,#007aff,#5ac8fa);">
          ${u.name.slice(0, 1)}
        </div>

        <div style="flex:1;min-width:0;">
          <div style="font-size:13px;font-weight:830;color:#1d1d1f;">${u.name}</div>
          <div style="font-size:12px;font-weight:650;color:rgba(29,29,31,0.42);margin-top:3px;">${u.email} · ${u.role}</div>
        </div>

        <button class="friends-btn friends-btn-primary" data-add-user="${u.id}">
          <i data-lucide="user-plus"></i>
          添加
        </button>
      </div>
    `;
      }).join('');

      box.querySelectorAll('[data-add-user]').forEach(function(btn) {
        btn.addEventListener('click', function() {
          closeFriendModal('modal-add-friend');
          appToast('好友申请已发送');
        });
      });

      lucide.createIcons();
    }

    function renderGroupMembers(keyword) {
      var box = document.getElementById('friend-group-members');
      if (!box) return;

      var lower = (keyword || '').toLowerCase();

      var data = friendListData.filter(function(f) {
        return !lower ||
          f.name.toLowerCase().indexOf(lower) > -1 ||
          f.role.toLowerCase().indexOf(lower) > -1 ||
          f.department.toLowerCase().indexOf(lower) > -1;
      });

      box.innerHTML = data.map(function(f) {
        var selected = friendState.selectedGroupMembers.has(f.id);

        return `
      <div class="friend-member-item ${selected ? 'selected' : ''}" data-member="${f.id}">
        <div class="friend-avatar" style="width:40px;height:40px;background:linear-gradient(135deg,${f.color1},${f.color2});">
          ${f.avatar}
        </div>

        <div style="flex:1;min-width:0;">
          <div style="font-size:13px;font-weight:830;color:#1d1d1f;">${f.name}</div>
          <div style="font-size:12px;font-weight:650;color:rgba(29,29,31,0.42);margin-top:3px;">${f.role} · ${f.department}</div>
        </div>

        <div style="width:24px;height:24px;border-radius:999px;display:flex;align-items:center;justify-content:center;${selected ? 'background:#007aff;color:#fff;' : 'border:1px solid rgba(0,122,255,0.16);'}">
          ${selected ? '<i data-lucide="check" style="width:14px;height:14px;"></i>' : ''}
        </div>
      </div>
    `;
      }).join('');

      box.querySelectorAll('[data-member]').forEach(function(item) {
        item.addEventListener('click', function() {
          var id = Number(this.dataset.member);

          if (friendState.selectedGroupMembers.has(id)) {
            friendState.selectedGroupMembers.delete(id);
          } else {
            friendState.selectedGroupMembers.add(id);
          }

          setText('friend-group-selected-count', friendState.selectedGroupMembers.size);
          renderGroupMembers(document.getElementById('friend-group-search').value.trim());
        });
      });

      lucide.createIcons();
    }

    function openFriendMorePopover(trigger) {
      var popover = document.getElementById('friend-more-popover');
      if (!popover || !trigger) return;

      var f = getCurrentFriend();

      var avatar = document.getElementById('friend-more-avatar');
      var name = document.getElementById('friend-more-name');
      var muteText = document.getElementById('friend-more-mute-text');

      if (avatar) {
        avatar.textContent = f.avatar;
        avatar.style.background = 'linear-gradient(135deg,' + f.color1 + ',' + f.color2 + ')';
      }

      if (name) {
        name.textContent = f.name;
      }

      if (muteText) {
        muteText.textContent = f.muted ? '关闭免打扰' : '设置免打扰';
      }

      popover.classList.add('show');

      var rect = trigger.getBoundingClientRect();
      var popWidth = 238;
      var gap = 10;

      var left = rect.right - popWidth;
      var top = rect.bottom + gap;

      if (left < 12) left = 12;
      if (left + popWidth > window.innerWidth - 12) {
        left = window.innerWidth - popWidth - 12;
      }

      if (top + 310 > window.innerHeight) {
        top = rect.top - 310 - gap;
      }

      if (top < 12) top = 12;

      popover.style.left = left + 'px';
      popover.style.top = top + 'px';

      lucide.createIcons();
    }

    function closeFriendMorePopover() {
      var popover = document.getElementById('friend-more-popover');
      if (popover) popover.classList.remove('show');
    }

    function friendSetTop() {
      var f = getCurrentFriend();

      friendListData = friendListData.filter(function(item) {
        return item.id !== f.id;
      });

      friendListData.unshift(f);
      friendState.currentId = f.id;

      renderFriendList();

      appToast('已置顶当前会话');
    }

    function friendMuteChat() {
      var f = getCurrentFriend();

      f.muted = !f.muted;

      renderFriendList();

      appToast(f.muted ? '已开启免打扰' : '已关闭免打扰');
    }

    function openFriendSearchPanel() {
      var f = getCurrentFriend();

      appAlert(
        `
      <div style="display:grid;gap:12px;">
        <input
          id="friend-message-search-input"
          class="friend-modal-input"
          placeholder="输入关键词搜索聊天记录..."
          style="height:42px;"
        />

        <div
          id="friend-message-search-result"
          style="max-height:220px;overflow-y:auto;display:grid;gap:8px;"
        >
          <div style="padding:16px;text-align:center;color:rgba(29,29,31,0.42);font-size:13px;font-weight:700;">
            输入关键词后显示匹配消息
          </div>
        </div>
      </div>
    `,
        {
          title: '搜索聊天记录',
          type: 'info',
          icon: 'search',
          html: true
        }
      );

      setTimeout(function() {
        var input = document.getElementById('friend-message-search-input');
        var result = document.getElementById('friend-message-search-result');

        if (!input || !result) return;

        input.focus();

        input.addEventListener('input', function() {
          var keyword = this.value.trim().toLowerCase();

          if (!keyword) {
            result.innerHTML = `
          <div style="padding:16px;text-align:center;color:rgba(29,29,31,0.42);font-size:13px;font-weight:700;">
            输入关键词后显示匹配消息
          </div>
        `;
            return;
          }

          var matched = f.messages.filter(function(m) {
            return (m.text || m.fileName || '').toLowerCase().indexOf(keyword) > -1;
          });

          if (!matched.length) {
            result.innerHTML = `
          <div style="padding:16px;text-align:center;color:rgba(29,29,31,0.42);font-size:13px;font-weight:700;">
            没有找到相关消息
          </div>
        `;
            return;
          }

          result.innerHTML = matched.map(function(m) {
            var sender = m.from === 'me' ? '我' : f.name;
            var text = m.text || m.fileName || '';

            return `
          <div style="
            padding:10px 12px;
            border-radius:14px;
            background:rgba(255,255,255,0.72);
            border:1px solid rgba(0,122,255,0.10);
            text-align:left;
          ">
            <div style="font-size:12px;font-weight:850;color:#007aff;margin-bottom:4px;">${sender}</div>
            <div style="font-size:13px;font-weight:650;color:rgba(29,29,31,0.72);line-height:1.5;">${escapeHTML(text)}</div>
          </div>
        `;
          }).join('');
        });
      }, 30);
    }

    function friendExportChat() {
      var f = getCurrentFriend();

      if (!f.messages.length) {
        appToast('暂无聊天记录可导出');
        return;
      }

      var text = f.messages.map(function(m) {
        var sender = m.from === 'me' ? '我' : f.name;

        if (m.type === 'file') {
          return sender + '：[文件] ' + m.fileName;
        }

        if (m.type === 'image') {
          return sender + '：[图片] ' + m.fileName;
        }

        return sender + '：' + (m.text || '');
      }).join('\n');

      var blob = new Blob([text], {
        type: 'text/plain;charset=utf-8'
      });

      var url = URL.createObjectURL(blob);

      var a = document.createElement('a');
      a.href = url;
      a.download = f.name + '-聊天记录.txt';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      URL.revokeObjectURL(url);

      appToast('聊天记录已导出');
    }