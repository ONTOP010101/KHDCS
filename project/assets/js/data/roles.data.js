export const roleListData = [
  {
    id: 1,
    name: '系统管理员',
    code: 'admin',
    scope: '全部数据',
    users: 3,
    status: 'enabled',
    desc: '拥有系统全部管理权限',
    createTime: '2024-01-12',
    permissions: ['dashboard', 'sample', 'gallery', 'friends', 'roles', 'users', 'add', 'edit', 'delete', 'export', 'all-data']
  },
  {
    id: 2,
    name: '设计主管',
    code: 'design_manager',
    scope: '本部门数据',
    users: 8,
    status: 'enabled',
    desc: '负责设计部门样品与图库管理',
    createTime: '2024-02-08',
    permissions: ['dashboard', 'sample', 'gallery', 'add', 'edit', 'export', 'dept-data']
  },
  {
    id: 3,
    name: '摄影师',
    code: 'photographer',
    scope: '本人数据',
    users: 12,
    status: 'enabled',
    desc: '负责图片上传与图片资料维护',
    createTime: '2024-03-16',
    permissions: ['gallery', 'add', 'edit', 'self-data']
  },
  {
    id: 4,
    name: '数据审核员',
    code: 'data_auditor',
    scope: '自定义数据',
    users: 5,
    status: 'disabled',
    desc: '负责样品数据审核与校验',
    createTime: '2024-04-21',
    permissions: ['sample', 'export', 'custom-data']
  }
];

export const roleMemberMock = [
  { name: 'Alex', account: 'alex', dept: '设计部' },
  { name: 'Bella', account: 'bella', dept: '摄影部' },
  { name: 'Chris', account: 'chris', dept: '运营部' },
  { name: 'Diana', account: 'diana', dept: '数据部' },
  { name: 'Eric', account: 'eric', dept: '技术部' }
];
