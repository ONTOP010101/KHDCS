export const friendState = {
  currentId: 1,
  filterStatus: 'all',
  search: '',
  selectedGroupMembers: new Set()
};

export const friendListData = [
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
