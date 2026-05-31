<template>
  <div class="friends-page">
    <div class="friends-card friends-left">
      <div class="friends-left-head">
        <div class="friends-title-row">
          <div class="friends-title">
            <div class="friends-title-icon">
              <MessageCircle :size="18" />
            </div>
            <div>
              <strong>消息</strong>
              <span>好友/群聊</span>
            </div>
          </div>
          <div class="friends-head-actions">
            <button class="friends-btn friends-btn-purple" @click="showCreateGroupModal = true">
              <UsersRound :size="15" /> 创建群聊
            </button>
            <button class="friends-btn friends-btn-primary" @click="showAddFriendModal = true">
              <UserPlus :size="15" /> 添加好友
            </button>
          </div>
        </div>
        <div class="friends-search">
          <Search :size="16" />
          <input v-model="searchText" placeholder="搜索好友..." />
        </div>
      </div>
      <div class="friends-tabs">
        <button class="friends-tab" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">全部</button>
        <button class="friends-tab" :class="{ active: activeTab === 'online' }" @click="activeTab = 'online'">在线</button>
        <button class="friends-tab" :class="{ active: activeTab === 'away' }" @click="activeTab = 'away'">离开</button>
        <button class="friends-tab" :class="{ active: activeTab === 'offline' }" @click="activeTab = 'offline'">离线</button>
      </div>
      <div class="friends-list">
        <div
          v-for="f in filteredFriends"
          :key="f.id"
          class="friend-item"
          :class="{ active: currentFriendId === f.id }"
          @click="selectFriend(f)"
        >
          <div class="friend-avatar" :style="{ background: f.color }">
            {{ f.name[0] }}
            <span class="friend-status-dot" :class="f.status"></span>
          </div>
          <div class="friend-main">
            <div class="friend-row">
              <span class="friend-name">{{ f.name }}</span>
              <span class="friend-time">{{ f.time }}</span>
            </div>
            <div class="friend-preview">{{ f.preview }}</div>
          </div>
          <span v-if="f.unread" class="friend-unread">{{ f.unread }}</span>
        </div>
      </div>
    </div>

    <div class="friends-card friends-chat">
      <template v-if="currentFriend">
        <div class="friend-chat-header">
          <div class="friend-avatar" :style="{ background: currentFriend.color, width: '38px', height: '38px', borderRadius: '15px', fontSize: '14px' }">
            {{ currentFriend.name[0] }}
          </div>
          <div class="friend-chat-title">
            <strong>{{ currentFriend.name }}</strong>
            <span>{{ statusLabel(currentFriend.status) }} · {{ currentFriend.role }} · {{ currentFriend.department }}</span>
          </div>
          <div class="friend-chat-actions">
            <button class="friend-icon-btn" @click="showProfileModal = true">
              <User :size="18" />
            </button>
            <button class="friend-icon-btn">
              <Phone :size="18" />
            </button>
            <button class="friend-icon-btn">
              <Video :size="18" />
            </button>
            <button class="friend-icon-btn" @click.stop="toggleMorePopover">
              <MoreHorizontal :size="18" />
            </button>
          </div>
        </div>

        <div class="friend-chat-messages" ref="messagesRef">
          <div class="friend-date-line"><span>今天</span></div>
          <template v-for="(msg, idx) in currentMessages" :key="idx">
            <div class="friend-msg" :class="msg.from">
              <div v-if="msg.from === 'other'" class="friend-msg-avatar" :style="{ background: currentFriend.color }">
                {{ currentFriend.name[0] }}
              </div>
              <div v-if="msg.type === 'text'" class="friend-bubble">{{ msg.content }}</div>
              <div v-else-if="msg.type === 'file'" class="friend-bubble friend-file-bubble">
                <div class="friend-file-icon"><FileText :size="18" /></div>
                <div class="friend-file-info">
                  <strong>{{ msg.fileName }}</strong>
                  <span>{{ msg.fileSize }}</span>
                </div>
              </div>
              <div v-else-if="msg.type === 'image'" class="friend-bubble">
                <img :src="msg.src" style="max-width:200px;border-radius:12px" />
              </div>
              <div v-if="msg.from === 'me'" class="friend-msg-avatar" style="background:linear-gradient(135deg,#0a84ff,#5ac8fa)">
                我
              </div>
            </div>
          </template>
        </div>

        <div class="friend-typing" :class="{ show: isTyping }">{{ currentFriend.name }} 正在输入...</div>

        <div class="friend-input-area">
          <div class="friend-emoji-panel" :class="{ show: showEmoji }">
            <div class="friend-emoji-grid">
              <button v-for="e in emojis" :key="e" @click="insertEmoji(e)">{{ e }}</button>
            </div>
          </div>
          <div class="friend-input-row">
            <button class="friend-icon-btn" @click="showEmoji = !showEmoji">
              <Smile :size="18" />
            </button>
            <button class="friend-icon-btn" @click="$refs.fileInput.click()">
              <Paperclip :size="18" />
            </button>
            <button class="friend-icon-btn" @click="$refs.imageInput.click()">
              <Image :size="18" />
            </button>
            <textarea
              v-model="inputText"
              placeholder="输入消息..."
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.enter.shift="() => {}"
            ></textarea>
            <button class="friends-btn friends-btn-primary" @click="sendMessage">
              <Send :size="15" /> 发送
            </button>
          </div>
          <input ref="fileInput" type="file" hidden @change="onFileAttach" />
          <input ref="imageInput" type="file" accept="image/*" hidden @change="onImageAttach" />
        </div>
      </template>
      <template v-else>
        <div style="flex:1;display:flex;align-items:center;justify-content:center;color:rgba(29,29,31,0.3);font-size:15px;font-weight:700">
          选择一个好友开始聊天
        </div>
      </template>
    </div>

    <div class="friends-card friends-right" v-if="currentFriend">
      <div class="friend-profile-panel">
        <div class="friend-profile-avatar" :style="{ background: currentFriend.color }">
          {{ currentFriend.name[0] }}
        </div>
        <div class="friend-profile-name">{{ currentFriend.name }}</div>
        <div class="friend-profile-role">{{ currentFriend.role }}</div>
        <div class="friend-profile-badge" :class="currentFriend.status">{{ statusLabel(currentFriend.status) }}</div>
      </div>
      <div class="friend-profile-body">
        <div class="friend-info-row">
          <span>好友ID</span>
          <strong>{{ currentFriend.id }}</strong>
        </div>
        <div class="friend-info-row">
          <span>部门</span>
          <strong>{{ currentFriend.department }}</strong>
        </div>
        <div class="friend-info-row">
          <span>加入时间</span>
          <strong>{{ currentFriend.joinDate }}</strong>
        </div>
        <div class="friend-info-row">
          <span>最近在线</span>
          <strong>{{ currentFriend.lastOnline }}</strong>
        </div>
        <div class="friend-stat-grid">
          <div class="friend-stat-card">
            <span>消息数</span>
            <strong>{{ currentFriend.msgCount }}</strong>
          </div>
          <div class="friend-stat-card">
            <span>文件数</span>
            <strong>{{ currentFriend.fileCount }}</strong>
          </div>
        </div>
        <div class="friend-profile-actions">
          <button class="friends-btn friends-btn-primary" style="width:100%">
            <Phone :size="15" /> 发起通话
          </button>
          <button class="friends-btn friends-btn-ghost" style="width:100%" @click="clearChat">
            <Eraser :size="15" /> 清空聊天
          </button>
          <button class="friends-btn friends-btn-ghost" style="width:100%" @click="toggleBlock">
            <Ban :size="15" /> {{ currentFriend.blocked ? '取消拉黑' : '拉黑好友' }}
          </button>
          <button class="friends-btn friends-btn-danger" style="width:100%" @click="deleteFriend">
            <UserMinus :size="15" /> 删除好友
          </button>
        </div>
      </div>
    </div>

    <div class="friend-more-popover" :class="{ show: showMore }" :style="moreStyle">
      <div class="friend-more-head">
        <div class="friend-more-avatar" :style="{ background: currentFriend?.color }">{{ currentFriend?.name?.[0] }}</div>
        <div class="friend-more-title">
          <strong>{{ currentFriend?.name }}</strong>
          <span>{{ statusLabel(currentFriend?.status) }}</span>
        </div>
      </div>
      <div class="friend-more-divider"></div>
      <button class="friend-more-item" @click="showMore = false">
        <Pin :size="17" /> 置顶会话
      </button>
      <button class="friend-more-item" @click="showMore = false">
        <BellOff :size="17" /> 设置免打扰
      </button>
      <button class="friend-more-item" @click="searchChat">
        <Search :size="17" /> 搜索聊天记录
      </button>
      <button class="friend-more-item" @click="exportChat">
        <Download :size="17" /> 导出聊天记录
      </button>
      <button class="friend-more-item danger" @click="clearChatFromMore">
        <Eraser :size="17" /> 清空聊天记录
      </button>
    </div>

    <div class="friend-modal-mask" :class="{ show: showAddFriendModal }" @click.self="showAddFriendModal = false">
      <div class="friend-modal">
        <div class="friend-modal-head">
          <strong>添加好友</strong>
          <button class="friend-icon-btn" @click="showAddFriendModal = false"><X :size="18" /></button>
        </div>
        <div class="friend-modal-body">
          <input class="friend-modal-input" v-model="addFriendSearch" placeholder="搜索用户..." />
          <div class="friend-result-list">
            <div v-for="r in addFriendResults" :key="r.id" class="friend-result-item">
              <div class="friend-avatar" :style="{ background: r.color, width: '36px', height: '36px', borderRadius: '14px', fontSize: '13px' }">
                {{ r.name[0] }}
              </div>
              <div class="friend-main">
                <span class="friend-name">{{ r.name }}</span>
                <span class="friend-preview">{{ r.role }}</span>
              </div>
              <button class="friends-btn friends-btn-primary" @click="addFriend(r)">
                <UserPlus :size="14" /> 添加
              </button>
            </div>
          </div>
        </div>
        <div class="friend-modal-foot">
          <button class="friends-btn friends-btn-ghost" @click="showAddFriendModal = false">关闭</button>
        </div>
      </div>
    </div>

    <div class="friend-modal-mask" :class="{ show: showCreateGroupModal }" @click.self="showCreateGroupModal = false">
      <div class="friend-modal">
        <div class="friend-modal-head">
          <strong>创建群聊</strong>
          <button class="friend-icon-btn" @click="showCreateGroupModal = false"><X :size="18" /></button>
        </div>
        <div class="friend-modal-body">
          <input class="friend-modal-input" v-model="groupName" placeholder="群聊名称" />
          <div style="margin-top:12px">
            <input class="friend-modal-input" v-model="memberSearch" placeholder="搜索成员..." />
          </div>
          <div class="friend-member-list">
            <div
              v-for="f in friends"
              :key="f.id"
              class="friend-member-item"
              :class="{ selected: selectedMembers.includes(f.id) }"
              @click="toggleMember(f.id)"
            >
              <div class="friend-avatar" :style="{ background: f.color, width: '32px', height: '32px', borderRadius: '13px', fontSize: '12px' }">
                {{ f.name[0] }}
              </div>
              <div class="friend-main">
                <span class="friend-name">{{ f.name }}</span>
              </div>
              <component :is="selectedMembers.includes(f.id) ? Check : X" :size="16" style="flex-shrink:0" />
            </div>
          </div>
        </div>
        <div class="friend-modal-foot">
          <button class="friends-btn friends-btn-ghost" @click="showCreateGroupModal = false">取消</button>
          <button class="friends-btn friends-btn-primary" @click="createGroup">
            <UsersRound :size="14" /> 创建
          </button>
        </div>
      </div>
    </div>

    <div class="friend-modal-mask" :class="{ show: showProfileModal }" @click.self="showProfileModal = false">
      <div class="friend-modal" style="width:min(400px,calc(100vw - 40px))">
        <div class="friend-modal-head">
          <strong>好友资料</strong>
          <button class="friend-icon-btn" @click="showProfileModal = false"><X :size="18" /></button>
        </div>
        <div class="friend-modal-body" style="text-align:center;padding:30px 18px" v-if="currentFriend">
          <div class="friend-profile-avatar" :style="{ background: currentFriend.color, margin: '0 auto 14px' }">
            {{ currentFriend.name[0] }}
          </div>
          <div class="friend-profile-name">{{ currentFriend.name }}</div>
          <div class="friend-profile-role">{{ currentFriend.role }} · {{ currentFriend.department }}</div>
          <div class="friend-profile-badge" :class="currentFriend.status" style="margin-top:12px">
            {{ statusLabel(currentFriend.status) }}
          </div>
          <div style="margin-top:20px;text-align:left">
            <div class="friend-info-row">
              <span>好友ID</span>
              <strong>{{ currentFriend.id }}</strong>
            </div>
            <div class="friend-info-row">
              <span>部门</span>
              <strong>{{ currentFriend.department }}</strong>
            </div>
            <div class="friend-info-row">
              <span>加入时间</span>
              <strong>{{ currentFriend.joinDate }}</strong>
            </div>
            <div class="friend-info-row">
              <span>最近在线</span>
              <strong>{{ currentFriend.lastOnline }}</strong>
            </div>
          </div>
        </div>
        <div class="friend-modal-foot">
          <button class="friends-btn friends-btn-ghost" @click="showProfileModal = false">关闭</button>
        </div>
      </div>
    </div>

    <div class="friend-modal-mask" :class="{ show: showSearchModal }" @click.self="showSearchModal = false">
      <div class="friend-modal">
        <div class="friend-modal-head">
          <strong>搜索聊天记录</strong>
          <button class="friend-icon-btn" @click="showSearchModal = false"><X :size="18" /></button>
        </div>
        <div class="friend-modal-body">
          <input class="friend-modal-input" v-model="chatSearchText" placeholder="输入关键词..." />
          <div class="friend-result-list" style="margin-top:12px">
            <div v-for="(msg, idx) in searchedMessages" :key="idx" class="friend-result-item" style="flex-direction:column;align-items:flex-start;gap:4px">
              <span style="font-size:12px;color:rgba(29,29,31,0.4);font-weight:700">{{ msg.from === 'me' ? '我' : currentFriend?.name }}</span>
              <span style="font-size:13px;color:#1d1d1f;font-weight:700">{{ msg.content }}</span>
            </div>
          </div>
        </div>
        <div class="friend-modal-foot">
          <button class="friends-btn friends-btn-ghost" @click="showSearchModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import '@/styles/friends.css'
import {
  MessageCircle, UsersRound, UserPlus, Search, Phone, Video, MoreHorizontal,
  Smile, Paperclip, Image, Send, User, Ban, Eraser, UserMinus, X, Check,
  FileText, Pin, BellOff, Download, ShieldCheck
} from 'lucide-vue-next'

const friends = reactive([
  { id: 1, name: 'Alex', color: 'linear-gradient(135deg,#0a84ff,#5ac8fa)', status: 'online', role: '前端工程师', department: '技术部', time: '10:32', preview: '你好，项目进展如何？', unread: 3, joinDate: '2024-03-15', lastOnline: '刚刚', msgCount: 128, fileCount: 12, blocked: false },
  { id: 2, name: 'Bella', color: 'linear-gradient(135deg,#af52de,#5856d6)', status: 'online', role: '产品经理', department: '产品部', time: '10:15', preview: '需求文档已更新', unread: 1, joinDate: '2024-02-20', lastOnline: '5分钟前', msgCount: 256, fileCount: 34, blocked: false },
  { id: 3, name: 'Chris', color: 'linear-gradient(135deg,#ff9500,#ff3b30)', status: 'away', role: 'UI设计师', department: '设计部', time: '09:48', preview: '设计稿完成了', unread: 0, joinDate: '2024-01-10', lastOnline: '30分钟前', msgCount: 89, fileCount: 56, blocked: false },
  { id: 4, name: 'Diana', color: 'linear-gradient(135deg,#34c759,#30d158)', status: 'offline', role: '后端工程师', department: '技术部', time: '昨天', preview: 'API接口已部署', unread: 0, joinDate: '2024-04-01', lastOnline: '昨天 18:30', msgCount: 67, fileCount: 8, blocked: false },
  { id: 5, name: 'Eric', color: 'linear-gradient(135deg,#5856d6,#007aff)', status: 'online', role: '测试工程师', department: '质量部', time: '08:55', preview: '发现了几个Bug', unread: 5, joinDate: '2024-05-12', lastOnline: '刚刚', msgCount: 312, fileCount: 23, blocked: false },
  { id: 6, name: 'Fiona', color: 'linear-gradient(135deg,#ff2d55,#af52de)', status: 'away', role: '项目经理', department: '管理部', time: '昨天', preview: '明天开会讨论', unread: 0, joinDate: '2024-01-05', lastOnline: '2小时前', msgCount: 45, fileCount: 5, blocked: false }
])

const messagesMap = reactive({
  1: [
    { from: 'other', type: 'text', content: '你好，项目进展如何？' },
    { from: 'me', type: 'text', content: '进展顺利，前端部分已经完成了80%' },
    { from: 'other', type: 'text', content: '太好了！有什么需要我帮忙的吗？' },
    { from: 'me', type: 'file', fileName: '项目进度报告.pdf', fileSize: '2.4 MB' },
    { from: 'other', type: 'text', content: '收到，我看看' }
  ],
  2: [
    { from: 'other', type: 'text', content: '需求文档已更新' },
    { from: 'me', type: 'text', content: '好的，我马上看' },
    { from: 'other', type: 'file', fileName: 'PRD_v2.3.docx', fileSize: '1.8 MB' },
    { from: 'me', type: 'text', content: '收到，有几个问题想确认一下' }
  ],
  3: [
    { from: 'other', type: 'text', content: '设计稿完成了' },
    { from: 'me', type: 'text', content: '太棒了！发我看看' },
    { from: 'other', type: 'image', src: 'https://picsum.photos/seed/chris-design/300/200' },
    { from: 'me', type: 'text', content: '效果很好，配色很舒服' }
  ],
  4: [
    { from: 'other', type: 'text', content: 'API接口已部署' },
    { from: 'me', type: 'text', content: '好的，我来对接测试一下' },
    { from: 'other', type: 'text', content: '有问题随时找我' }
  ],
  5: [
    { from: 'other', type: 'text', content: '发现了几个Bug' },
    { from: 'me', type: 'text', content: '哪些模块的？' },
    { from: 'other', type: 'file', fileName: 'Bug报告_0523.xlsx', fileSize: '856 KB' },
    { from: 'me', type: 'text', content: '我看看，优先级高的我先处理' }
  ],
  6: [
    { from: 'other', type: 'text', content: '明天开会讨论' },
    { from: 'me', type: 'text', content: '几点？在哪个会议室？' },
    { from: 'other', type: 'text', content: '上午10点，3号会议室' }
  ]
})

const autoReplies = [
  '好的，收到！',
  '我稍后回复你',
  '没问题，我来处理',
  '了解了，谢谢提醒',
  '这个方案不错',
  '我看看再说',
  '马上处理',
  '好的，我确认一下'
]

const emojis = [
  '😀','😂','🤣','😊','😍','🥰','😘','😜','🤗','🤔',
  '😎','🥳','😇','🤩','😋','🤤','😴','😷','🤮','🥵',
  '👍','👎','👏','🙌','🤝','✌️','🤞','💪','❤️','💔',
  '🔥','⭐','🎉','🎊','💯','✅','❌','⚡','💡','🎯'
]

const searchText = ref('')
const activeTab = ref('all')
const currentFriendId = ref(null)
const inputText = ref('')
const showEmoji = ref(false)
const isTyping = ref(false)
const showMore = ref(false)
const moreStyle = reactive({ top: '0px', left: '0px' })
const showAddFriendModal = ref(false)
const showCreateGroupModal = ref(false)
const showProfileModal = ref(false)
const showSearchModal = ref(false)
const addFriendSearch = ref('')
const groupName = ref('')
const memberSearch = ref('')
const selectedMembers = ref([])
const chatSearchText = ref('')
const messagesRef = ref(null)

const addFriendResults = reactive([
  { id: 101, name: 'Grace', color: 'linear-gradient(135deg,#00c7be,#32ade6)', role: '运维工程师' },
  { id: 102, name: 'Henry', color: 'linear-gradient(135deg,#ff6b6b,#ee5a24)', role: '数据分析师' },
  { id: 103, name: 'Ivy', color: 'linear-gradient(135deg,#a29bfe,#6c5ce7)', role: '架构师' }
])

const currentFriend = computed(() => friends.find(f => f.id === currentFriendId.value) || null)

const currentMessages = computed(() => messagesMap[currentFriendId.value] || [])

const filteredFriends = computed(() => {
  let list = friends
  if (activeTab.value !== 'all') {
    list = list.filter(f => f.status === activeTab.value)
  }
  if (searchText.value) {
    const kw = searchText.value.toLowerCase()
    list = list.filter(f => f.name.toLowerCase().includes(kw) || f.preview.toLowerCase().includes(kw))
  }
  return list
})

const searchedMessages = computed(() => {
  if (!chatSearchText.value) return []
  const msgs = messagesMap[currentFriendId.value] || []
  return msgs.filter(m => m.type === 'text' && m.content.includes(chatSearchText.value))
})

const statusLabel = (status) => {
  const map = { online: '在线', away: '离开', offline: '离线' }
  return map[status] || '未知'
}

const selectFriend = (f) => {
  currentFriendId.value = f.id
  f.unread = 0
  showEmoji.value = false
  showMore.value = false
  nextTick(() => scrollToBottom())
}

const sendMessage = () => {
  const text = inputText.value.trim()
  if (!text || !currentFriendId.value) return
  if (!messagesMap[currentFriendId.value]) messagesMap[currentFriendId.value] = []
  messagesMap[currentFriendId.value].push({ from: 'me', type: 'text', content: text })
  inputText.value = ''
  showEmoji.value = false
  nextTick(() => scrollToBottom())
  simulateReply()
}

const simulateReply = () => {
  isTyping.value = true
  setTimeout(() => {
    isTyping.value = false
    if (!currentFriendId.value) return
    const reply = autoReplies[Math.floor(Math.random() * autoReplies.length)]
    messagesMap[currentFriendId.value].push({ from: 'other', type: 'text', content: reply })
    nextTick(() => scrollToBottom())
  }, 900)
}

const insertEmoji = (emoji) => {
  inputText.value += emoji
}

const onFileAttach = (e) => {
  const file = e.target.files[0]
  if (!file || !currentFriendId.value) return
  messagesMap[currentFriendId.value].push({
    from: 'me',
    type: 'file',
    fileName: file.name,
    fileSize: (file.size / 1024).toFixed(1) + ' KB'
  })
  e.target.value = ''
  nextTick(() => scrollToBottom())
}

const onImageAttach = (e) => {
  const file = e.target.files[0]
  if (!file || !currentFriendId.value) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    messagesMap[currentFriendId.value].push({
      from: 'me',
      type: 'image',
      src: ev.target.result
    })
    nextTick(() => scrollToBottom())
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const toggleMorePopover = (e) => {
  showMore.value = !showMore.value
  if (showMore.value) {
    const rect = e.currentTarget.getBoundingClientRect()
    moreStyle.top = (rect.bottom + 6) + 'px'
    moreStyle.left = Math.max(8, rect.left - 160) + 'px'
  }
}

const searchChat = () => {
  showMore.value = false
  chatSearchText.value = ''
  showSearchModal.value = true
}

const exportChat = () => {
  showMore.value = false
  if (!currentFriend.value) return
  const msgs = messagesMap[currentFriendId.value] || []
  let text = `与 ${currentFriend.value.name} 的聊天记录\n${'='.repeat(40)}\n\n`
  msgs.forEach(m => {
    const who = m.from === 'me' ? '我' : currentFriend.value.name
    if (m.type === 'text') text += `[${who}] ${m.content}\n\n`
    else if (m.type === 'file') text += `[${who}] [文件] ${m.fileName} (${m.fileSize})\n\n`
    else if (m.type === 'image') text += `[${who}] [图片]\n\n`
  })
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `聊天记录_${currentFriend.value.name}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

const clearChat = () => {
  if (!currentFriendId.value) return
  if (!confirm('确认清空与该好友的聊天记录？')) return
  messagesMap[currentFriendId.value] = []
}

const clearChatFromMore = () => {
  showMore.value = false
  clearChat()
}

const toggleBlock = () => {
  if (!currentFriend.value) return
  currentFriend.value.blocked = !currentFriend.value.blocked
}

const deleteFriend = () => {
  if (!currentFriend.value) return
  if (!confirm(`确认删除好友 ${currentFriend.value.name}？`)) return
  const idx = friends.findIndex(f => f.id === currentFriendId.value)
  if (idx >= 0) {
    delete messagesMap[currentFriendId.value]
    friends.splice(idx, 1)
    currentFriendId.value = null
  }
}

const addFriend = (r) => {
  const exists = friends.find(f => f.id === r.id)
  if (exists) return
  friends.push({
    id: r.id,
    name: r.name,
    color: r.color,
    status: 'online',
    role: r.role,
    department: '新部门',
    time: '刚刚',
    preview: '新添加的好友',
    unread: 0,
    joinDate: new Date().toISOString().slice(0, 10),
    lastOnline: '刚刚',
    msgCount: 0,
    fileCount: 0,
    blocked: false
  })
  messagesMap[r.id] = []
  showAddFriendModal.value = false
}

const toggleMember = (id) => {
  const idx = selectedMembers.value.indexOf(id)
  if (idx >= 0) selectedMembers.value.splice(idx, 1)
  else selectedMembers.value.push(id)
}

const createGroup = () => {
  if (!groupName.value.trim()) return
  showCreateGroupModal.value = false
  groupName.value = ''
  selectedMembers.value = []
}

const onClickOutside = (e) => {
  if (showMore.value && !e.target.closest('.friend-more-popover') && !e.target.closest('.friend-icon-btn')) {
    showMore.value = false
  }
  if (showEmoji.value && !e.target.closest('.friend-emoji-panel') && !e.target.closest('.friend-icon-btn')) {
    showEmoji.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
})
</script>
